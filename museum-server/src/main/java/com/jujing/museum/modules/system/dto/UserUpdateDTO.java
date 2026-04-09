package com.jujing.museum.modules.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 修改用户请求
 */
@Data
public class UserUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    private String realName;

    private String phone;

    private String email;

    private Long deptId;

    /** 角色ID列表 */
    private List<Long> roleIds;
}
