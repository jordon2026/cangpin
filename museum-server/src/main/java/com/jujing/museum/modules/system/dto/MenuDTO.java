package com.jujing.museum.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单请求
 */
@Data
public class MenuDTO {

    @NotNull(message = "上级菜单不能为空")
    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    private String name;

    /** 类型：1目录 2菜单 3按钮 */
    private Integer type;

    private String path;

    private String component;

    private String permission;

    private String icon;

    private Integer sort;

    /** 是否可见：0可见 1隐藏 */
    private Integer visible;

    /** 状态：1-正常 0-禁用 */
    private Integer status;
}
