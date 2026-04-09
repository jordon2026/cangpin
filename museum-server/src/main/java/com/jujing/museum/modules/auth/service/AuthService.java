package com.jujing.museum.modules.auth.service;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jujing.museum.common.exception.BizException;
import com.jujing.museum.modules.auth.dto.*;
import com.jujing.museum.modules.auth.entity.SysMenu;
import com.jujing.museum.modules.auth.entity.SysRole;
import com.jujing.museum.modules.auth.entity.SysUser;
import com.jujing.museum.modules.auth.mapper.SysMenuMapper;
import com.jujing.museum.modules.auth.mapper.SysRoleMapper;
import com.jujing.museum.modules.auth.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final RedisConnectionFactory redisConnectionFactory;

    /** 获取独立的 StringRedisTemplate，避免与自定义 RedisTemplate 冲突 */
    private StringRedisTemplate getStringRedisTemplate() {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final String LOGIN_FAIL_PREFIX = "login_fail:";
    private static final String LOGIN_LOCK_PREFIX = "login_lock:";
    private static final int MAX_LOGIN_FAIL = 5;
    private static final int LOCK_MINUTES = 30;
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 生成算术验证码
     */
    public CaptchaVO generateCaptcha() {
        String uuid = IdUtil.simpleUUID();

        // 生成算术题
        int a = RandomUtil.randomInt(1, 30);
        int b = RandomUtil.randomInt(1, 30);
        String operator = RandomUtil.randomEle(new String[]{"+", "-",});
        int answer;
        String expression;

        if ("+".equals(operator)) {
            answer = a + b;
            expression = a + " + " + b + " = ?";
        } else {
            // 确保 a > b，结果为正数
            if (a < b) {
                int temp = a; a = b; b = temp;
            }
            answer = a - b;
            expression = a + " - " + b + " = ?";
        }

        // 绘制验证码图片
        String base64Img = drawCaptchaImage(expression, uuid);

        // 将答案存入 Redis
        getStringRedisTemplate().opsForValue().set(
                CAPTCHA_PREFIX + uuid,
                String.valueOf(answer),
                CAPTCHA_EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

        CaptchaVO vo = new CaptchaVO();
        vo.setUuid(uuid);
        vo.setImg("data:image/png;base64," + base64Img);
        return vo;
    }

    /**
     * 用户登录
     */
    public UserVO login(LoginDTO dto) {
        String username = dto.getUsername().trim();

        // 1. 检查是否被锁定
        String lockKey = LOGIN_LOCK_PREFIX + username;
        String lockValue = getStringRedisTemplate().opsForValue().get(lockKey);
        if (StrUtil.isNotBlank(lockValue)) {
            long remaining = getStringRedisTemplate().getExpire(lockKey, TimeUnit.MINUTES);
            throw new BizException("账号已锁定，请" + remaining + "分钟后再试");
        }

        // 2. 校验验证码
        String captchaKey = CAPTCHA_PREFIX + dto.getUuid();
        String captchaAnswer = getStringRedisTemplate().opsForValue().get(captchaKey);
        getStringRedisTemplate().delete(captchaKey); // 验证码用后即删
        if (StrUtil.isBlank(captchaAnswer)) {
            throw new BizException("验证码已过期，请刷新");
        }
        if (!captchaAnswer.equals(dto.getCaptchaCode().trim())) {
            throw new BizException("验证码错误");
        }

        // 3. 查询用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (user == null) {
            recordLoginFail(username);
            throw new BizException("用户名或密码错误");
        }

        // 4. 检查用户状态（1-正常 0-禁用）
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException("账号已被停用，请联系管理员");
        }

        // 5. 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            recordLoginFail(username);
            throw new BizException("用户名或密码错误");
        }

        // 6. 清除失败计数
        getStringRedisTemplate().delete(LOGIN_FAIL_PREFIX + username);
        getStringRedisTemplate().delete(LOGIN_LOCK_PREFIX + username);

        // 7. Sa-Token 登录
        StpUtil.login(user.getId());

        // 8. 构造返回
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setToken(StpUtil.getTokenValue());

        // 查询角色
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        vo.setRoles(roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList()));

        // 查询权限
        List<SysMenu> menus = menuMapper.selectMenusByUserId(user.getId());
        vo.setPermissions(menus.stream()
                .map(SysMenu::getPermission)
                .filter(p -> StrUtil.isNotBlank(p))
                .collect(Collectors.toList()));

        log.info("用户登录成功: {}", username);
        return vo;
    }

    /**
     * 退出登录
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 获取当前登录用户信息
     */
    public UserVO getCurrentUser() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            StpUtil.logout();
            throw new BizException(401, "用户不存在");
        }

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());

        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        vo.setRoles(roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList()));

        List<SysMenu> menus = menuMapper.selectMenusByUserId(user.getId());
        vo.setPermissions(menus.stream()
                .map(SysMenu::getPermission)
                .filter(p -> StrUtil.isNotBlank(p))
                .collect(Collectors.toList()));

        return vo;
    }

    /**
     * 获取当前用户菜单树
     */
    public List<SysMenu> getMenuTree() {
        long userId = StpUtil.getLoginIdAsLong();
        return menuMapper.selectMenusByUserId(userId);
    }

    /**
     * 记录登录失败
     */
    private void recordLoginFail(String username) {
        String failKey = LOGIN_FAIL_PREFIX + username;
        Long failCount = getStringRedisTemplate().opsForValue().increment(failKey);
        if (failCount != null && failCount == 1) {
            getStringRedisTemplate().expire(failKey, LOCK_MINUTES, TimeUnit.MINUTES);
        }
        if (failCount != null && failCount >= MAX_LOGIN_FAIL) {
            // 锁定账号
            getStringRedisTemplate().opsForValue().set(
                    LOGIN_LOCK_PREFIX + username,
                    "locked",
                    LOCK_MINUTES,
                    TimeUnit.MINUTES
            );
            log.warn("账号被锁定: {}, 失败次数: {}", username, failCount);
        }
    }

    /**
     * 绘制验证码图片
     */
    private String drawCaptchaImage(String expression, String uuid) {
        int width = 160;
        int height = 60;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景 — 浅米色
        g.setColor(new Color(245, 245, 220));
        g.fillRect(0, 0, width, height);

        // 边框
        g.setColor(new Color(139, 69, 19));
        g.drawRect(0, 0, width - 1, height - 1);

        // 干扰线
        Random random = new Random(uuid.hashCode());
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(random.nextInt(200), random.nextInt(150), random.nextInt(150)));
            g.drawLine(random.nextInt(width), random.nextInt(height),
                    random.nextInt(width), random.nextInt(height));
        }

        // 干扰点
        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(200), random.nextInt(200)));
            g.fillOval(random.nextInt(width), random.nextInt(height), 2, 2);
        }

        // 绘制表达式文字
        g.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 28));
        g.setColor(new Color(62, 39, 35)); // 深褐色

        // 每个字符随机偏移
        char[] chars = expression.toCharArray();
        int x = 12;
        for (char c : chars) {
            g.drawString(String.valueOf(c), x, 38 + random.nextInt(6) - 3);
            x += 18 + random.nextInt(4);
        }

        g.dispose();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("生成验证码图片失败", e);
            throw new BizException("验证码生成失败");
        }
    }
}
