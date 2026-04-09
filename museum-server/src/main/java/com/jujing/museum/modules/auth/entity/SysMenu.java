package com.jujing.museum.modules.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jujing.museum.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private String name;

    /** 类型（1目录 2菜单 3按钮） */
    private Integer type;

    private String path;

    private String component;

    private String permission;

    private String icon;

    private Integer sort;

    /** 是否可见（0可见 1隐藏） */
    private Integer visible;

    /** 状态：1-正常 0-禁用 */
    private Integer status;
}
