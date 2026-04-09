package com.jujing.museum.modules.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jujing.museum.modules.auth.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户ID查询菜单列表（通过角色关联）
     */
    @Select("""
        SELECT DISTINCT m.* FROM sys_menu m
        INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
        INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
        WHERE ur.user_id = #{userId} AND m.deleted = 0
        ORDER BY m.sort ASC
    """)
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 查询所有菜单（管理员用 - 不限制状态，用于权限分配时显示完整菜单树）
     */
    @Select("""
        SELECT * FROM sys_menu
        WHERE deleted = 0
        ORDER BY sort ASC
    """)
    List<SysMenu> selectAllMenus();
}
