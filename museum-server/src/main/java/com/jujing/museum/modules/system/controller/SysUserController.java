package com.jujing.museum.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jujing.museum.annotation.OperLog;
import com.jujing.museum.common.exception.BizException;
import com.jujing.museum.common.result.PageResult;
import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.auth.entity.*;
import com.jujing.museum.modules.auth.mapper.*;
import com.jujing.museum.modules.system.dto.UserAddDTO;
import com.jujing.museum.modules.system.dto.UserUpdateDTO;
import com.jujing.museum.modules.system.vo.SysUserVO;
import com.jujing.museum.util.PasswordUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/v1/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysDeptMapper deptMapper;

    @Operation(summary = "分页查询用户列表")
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public R<PageResult<SysUserVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long deptId) {

        Page<SysUser> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), SysUser::getUsername, username);
        wrapper.like(StringUtils.hasText(phone), SysUser::getPhone, phone);
        wrapper.eq(status != null, SysUser::getStatus, status);
        wrapper.eq(deptId != null, SysUser::getDeptId, deptId);
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> result = userMapper.selectPage(pageParam, wrapper);

        List<SysUserVO> voList = result.getRecords().stream().map(this::toUserVO).collect(Collectors.toList());
        return R.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList));
    }

    @Operation(summary = "查询用户详情")
    @SaCheckPermission("system:user:query")
    @GetMapping("/{id}")
    public R<SysUserVO> getInfo(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return R.ok(toUserVO(user));
    }

    @Operation(summary = "新增用户")
    @SaCheckPermission("system:user:add")
    @OperLog(title = "用户管理", type = 1)
    @PostMapping
    public R<Void> add(@Valid @RequestBody UserAddDTO dto) {
        // 检查用户名唯一
        SysUser exist = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (exist != null) {
            throw new BizException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtil.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        user.setStatus(1);
        userMapper.insert(user);

        // 保存用户角色
        saveUserRoles(user.getId(), dto.getRoleIds());
        return R.ok();
    }

    @Operation(summary = "修改用户")
    @SaCheckPermission("system:user:edit")
    @OperLog(title = "用户管理", type = 2)
    @PutMapping
    public R<Void> update(@Valid @RequestBody UserUpdateDTO dto) {
        SysUser user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        // 不能修改admin
        if ("admin".equals(user.getUsername())) {
            throw new BizException("不允许修改管理员账号");
        }
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        userMapper.updateById(user);

        // 更新用户角色
        if (dto.getRoleIds() != null) {
            saveUserRoles(user.getId(), dto.getRoleIds());
        }
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @SaCheckPermission("system:user:delete")
    @OperLog(title = "用户管理", type = 3)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new BizException("不允许删除管理员账号");
        }
        if (user.getId().equals(StpUtil.getLoginIdAsLong())) {
            throw new BizException("不允许删除当前登录账号");
        }
        userMapper.deleteById(id);
        // 删除用户角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        return R.ok();
    }

    @Operation(summary = "重置密码")
    @SaCheckPermission("system:user:resetPwd")
    @OperLog(title = "重置密码", type = 2)
    @PutMapping("/resetPwd")
    public R<Void> resetPwd(@RequestBody java.util.Map<String, Object> map) {
        Long userId = Long.valueOf(map.get("userId").toString());
        // 【安全修复】重置密码不再硬编码 admin123，改为随机8位密码并通过安全渠道告知操作者
        String newPwd = generateRandomPassword();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setPassword(PasswordUtil.encode(newPwd));
        userMapper.updateById(user);
        // 将临时密码返回给有权限的操作者（前端应以不可复制/遮挡方式展示一次）
        return R.ok("密码已重置，临时密码: " + newPwd + "（请立即通知用户修改）", null);
    }

    @Operation(summary = "修改状态")
    @SaCheckPermission("system:user:edit")
    @OperLog(title = "用户管理", type = 2)
    @PutMapping("/status")
    public R<Void> updateStatus(@RequestBody java.util.Map<String, Object> map) {
        Long userId = Long.valueOf(map.get("userId").toString());
        Integer status = Integer.valueOf(map.get("status").toString());
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new BizException("不允许修改管理员状态");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return R.ok();
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        // 先删除旧的
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 再插入新的
        if (!CollectionUtils.isEmpty(roleIds)) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    private SysUserVO toUserVO(SysUser user) {
        SysUserVO vo = new SysUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setDeptId(user.getDeptId());
        vo.setCreateTime(user.getCreateTime());

        // 查询部门名
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }

        // 查询角色
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        vo.setRoleIds(roles.stream().map(SysRole::getId).collect(Collectors.toList()));
        vo.setRoleNames(roles.stream().map(SysRole::getRoleName).collect(Collectors.toList()));

        return vo;
    }

    /**
     * 生成随机8位密码（含大小写字母+数字+特殊字符）
     */
    private String generateRandomPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        String all = upper + lower + digits + special;
        java.util.Random random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        // 确保至少各包含一个
        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(special.charAt(random.nextInt(special.length())));
        for (int i = 4; i < 10; i++) {
            sb.append(all.charAt(random.nextInt(all.length())));
        }
        // 打乱顺序
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i]; chars[i] = chars[j]; chars[j] = tmp;
        }
        return new String(chars);
    }
}
