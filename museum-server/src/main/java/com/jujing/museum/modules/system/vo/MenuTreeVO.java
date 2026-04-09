package com.jujing.museum.modules.system.vo;

import lombok.Data;

import java.util.List;

/**
 * 菜单树形 VO
 */
@Data
public class MenuTreeVO {
    private Long id;
    private Long parentId;
    private String name;
    private Integer type;
    private String path;
    private String component;
    private String permission;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
    private List<MenuTreeVO> children;
}
