package com.jujing.museum.modules.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jujing.museum.modules.auth.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色菜单关联 Mapper
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
}
