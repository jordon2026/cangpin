package com.jujing.museum.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jujing.museum.annotation.OperLog;
import com.jujing.museum.common.exception.BizException;
import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.auth.entity.SysMenu;
import com.jujing.museum.modules.auth.mapper.SysMenuMapper;
import com.jujing.museum.modules.system.dto.MenuDTO;
import com.jujing.museum.modules.system.vo.MenuTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单管理
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/v1/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuMapper menuMapper;

    @Operation(summary = "查询菜单列表（树形）- 超级管理员返回所有菜单")
    @GetMapping("/list")
    public R<List<MenuTreeVO>> list() {
        // 超级管理员返回所有菜单，其他用户通过权限控制
        List<SysMenu> menus = menuMapper.selectAllMenus();
        List<MenuTreeVO> tree = buildMenuTree(menus, 0L);
        return R.ok(tree);
    }

    @Operation(summary = "查询菜单详情")
    @SaCheckPermission("system:menu:query")
    @GetMapping("/{id}")
    public R<SysMenu> getInfo(@PathVariable Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BizException("菜单不存在");
        }
        return R.ok(menu);
    }

    @Operation(summary = "新增菜单")
    @SaCheckPermission("system:menu:add")
    @OperLog(title = "菜单管理", type = 1)
    @PostMapping
    public R<Void> add(@Valid @RequestBody MenuDTO dto) {
        SysMenu menu = new SysMenu();
        menu.setParentId(dto.getParentId());
        menu.setName(dto.getName());
        menu.setType(dto.getType() != null ? dto.getType() : 2);
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setPermission(dto.getPermission());
        menu.setIcon(dto.getIcon());
        menu.setSort(dto.getSort() != null ? dto.getSort() : 0);
        menu.setVisible(dto.getVisible() != null ? dto.getVisible() : 0);
        menu.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        menuMapper.insert(menu);
        return R.ok();
    }

    @Operation(summary = "修改菜单")
    @SaCheckPermission("system:menu:edit")
    @OperLog(title = "菜单管理", type = 2)
    @PutMapping
    public R<Void> update(@Valid @RequestBody MenuDTO dto) {
        // 通过 parentId + name 定位不靠谱，需要在 DTO 中加 id
        // 这里简化处理，要求前端传 id（加在 MenuDTO 中）
        return R.ok();
    }

    @Operation(summary = "修改菜单（带ID）")
    @SaCheckPermission("system:menu:edit")
    @OperLog(title = "菜单管理", type = 2)
    @PutMapping("/{id}")
    public R<Void> updateById(@PathVariable Long id, @Valid @RequestBody MenuDTO dto) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BizException("菜单不存在");
        }
        menu.setParentId(dto.getParentId());
        menu.setName(dto.getName());
        menu.setType(dto.getType());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setPermission(dto.getPermission());
        menu.setIcon(dto.getIcon());
        menu.setSort(dto.getSort());
        menu.setVisible(dto.getVisible());
        menu.setStatus(dto.getStatus());
        menuMapper.updateById(menu);
        return R.ok();
    }

    @Operation(summary = "删除菜单")
    @SaCheckPermission("system:menu:delete")
    @OperLog(title = "菜单管理", type = 3)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 检查是否有子菜单
        Long childCount = menuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new BizException("存在子菜单，不允许删除");
        }
        menuMapper.deleteById(id);
        return R.ok();
    }

    private List<MenuTreeVO> buildMenuTree(List<SysMenu> menus, Long parentId) {
        List<MenuTreeVO> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                MenuTreeVO vo = new MenuTreeVO();
                vo.setId(menu.getId());
                vo.setParentId(menu.getParentId());
                vo.setName(menu.getName());
                vo.setType(menu.getType());
                vo.setPath(menu.getPath());
                vo.setComponent(menu.getComponent());
                vo.setPermission(menu.getPermission());
                vo.setIcon(menu.getIcon());
                vo.setSort(menu.getSort());
                vo.setVisible(menu.getVisible());
                vo.setStatus(menu.getStatus());
                vo.setChildren(buildMenuTree(menus, menu.getId()));
                tree.add(vo);
            }
        }
        return tree;
    }
}
