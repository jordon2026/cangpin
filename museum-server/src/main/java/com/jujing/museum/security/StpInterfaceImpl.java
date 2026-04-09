package com.jujing.museum.security;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jujing.museum.modules.auth.mapper.SysMenuMapper;
import com.jujing.museum.modules.auth.mapper.SysRoleMapper;
import com.jujing.museum.modules.auth.entity.SysRole;
import com.jujing.museum.modules.auth.entity.SysMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限接口实现
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    /**
     * 返回指定账号拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId);
        return menus.stream()
                .map(SysMenu::getPermission)
                .filter(p -> p != null && !p.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 返回指定账号拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);
        return roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
    }
}
