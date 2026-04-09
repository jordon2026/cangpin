package com.jujing.museum.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 部门请求
 */
@Data
public class DeptDTO {

    @NotNull(message = "上级部门不能为空")
    private Long parentId;

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    private Integer sort;

    private String leader;

    private String phone;

    private String email;

    /** 状态：1-正常 0-禁用 */
    private Integer status;
}
