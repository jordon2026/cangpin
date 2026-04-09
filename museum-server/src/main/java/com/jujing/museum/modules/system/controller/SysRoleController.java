package com.jujing.museum.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jujing.museum.annotation.OperLog;
import com.jujing.museum.common.exception.BizException;
import com.jujing.museum.common.result.PageResult;
import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.auth.entity.SysRole;
import com.jujing.museum.modules.auth.entity.SysRoleMenu;
import com.jujing.museum.modules.auth.mapper.SysRoleMapper;
import com.jujing.museum.modules.auth.mapper.SysRoleMenuMapper;
import com.jujing.museum.modules.system.dto.RoleDTO;
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
 * 角色管理
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/v1/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @Operation(summary = "查询所有角色（下拉用）")
    @SaCheckPermission("system:role:list")
    @GetMapping("/list")
    public R<List<SysRole>> list() {
        List<SysRole> roles = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 1).orderByAsc(SysRole::getSort));
        return R.ok(roles);
    }

    @Operation(summary = "分页查询角色")
    @SaCheckPermission("system:role:list")
    @GetMapping("/page")
    public R<PageResult<SysRole>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status) {

        Page<SysRole> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName);
        wrapper.like(StringUtils.hasText(roleCode), SysRole::getRoleCode, roleCode);
        wrapper.eq(status != null, SysRole::getStatus, status);
        wrapper.orderByAsc(SysRole::getSort);

        Page<SysRole> result = roleMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @Operation(summary = "新增角色")
    @SaCheckPermission("system:role:add")
    @OperLog(title = "角色管理", type = 1)
    @PostMapping
    public R<Void> add(@Valid @RequestBody RoleDTO dto) {
        // 检查编码唯一
        SysRole exist = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, dto.getRoleCode()));
        if (exist != null) {
            throw new BizException("角色编码已存在");
        }
        SysRole role = new SysRole();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setSort(dto.getSort());
        role.setStatus(1);
        role.setRemark(dto.getRemark());
        roleMapper.insert(role);

        // 保存角色菜单
        saveRoleMenus(role.getId(), dto.getMenuIds());
        return R.ok();
    }

    @Operation(summary = "修改角色")
    @SaCheckPermission("system:role:edit")
    @OperLog(title = "角色管理", type = 2)
    @PutMapping
    public R<Void> update(@Valid @RequestBody RoleDTO dto) {
        SysRole role = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, dto.getRoleCode()));
        if (role == null) {
            throw new BizException("角色不存在");
        }
        role.setRoleName(dto.getRoleName());
        role.setSort(dto.getSort());
        role.setRemark(dto.getRemark());
        roleMapper.updateById(role);

        if (dto.getMenuIds() != null) {
            saveRoleMenus(role.getId(), dto.getMenuIds());
        }
        return R.ok();
    }

    @Operation(summary = "删除角色")
    @SaCheckPermission("system:role:delete")
    @OperLog(title = "角色管理", type = 3)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        if ("admin".equals(role.getRoleCode())) {
            throw new BizException("不允许删除管理员角色");
        }
        roleMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        return R.ok();
    }

    @Operation(summary = "查询角色已分配的菜单ID列表")
    @SaCheckPermission("system:role:query")
    @GetMapping("/menuIds/{roleId}")
    public R<List<Long>> getMenuIds(@PathVariable Long roleId) {
        List<SysRoleMenu> list = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        List<Long> menuIds = list.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        return R.ok(menuIds);
    }

    @Operation(summary = "分配角色菜单权限")
    @SaCheckPermission("system:role:edit")
    @OperLog(title = "分配权限", type = 2)
    @PutMapping("/menus")
    public R<Void> assignMenus(@RequestBody java.util.Map<String, Object> map) {
        Long roleId = Long.valueOf(map.get("roleId").toString());
        @SuppressWarnings("unchecked")
        List<Long> menuIds = ((List<Number>) map.get("menuIds")).stream()
                .map(Number::longValue).collect(Collectors.toList());
        saveRoleMenus(roleId, menuIds);
        return R.ok();
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (!CollectionUtils.isEmpty(menuIds)) {
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
    }
}
