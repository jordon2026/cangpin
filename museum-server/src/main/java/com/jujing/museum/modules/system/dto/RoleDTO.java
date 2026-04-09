package com.jujing.museum.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 角色请求
 */
@Data
public class RoleDTO {

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    private Integer sort;

    private String remark;

    /** 菜单权限ID列表 */
    private List<Long> menuIds;
}
