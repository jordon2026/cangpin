package com.jujing.museum.modules.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jujing.museum.modules.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户ID查询角色列表
     */
    @Select("""
        SELECT r.* FROM sys_role r
        INNER JOIN sys_user_role ur ON r.id = ur.role_id
        WHERE ur.user_id = #{userId} AND r.deleted = 0 AND r.status = 1
    """)
    List<com.jujing.museum.modules.auth.entity.SysRole> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 执行原生SQL查询（用于统计）
     */
    @Select("${sql}")
    List<Map<String, Object>> execSql(@Param("sql") String sql);
}
