package com.jujing.museum.modules.collection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 藏品表
 */
@Data
@TableName("collection")
public class Collection {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 藏品编号 */
    private String collectionNo;

    /** 藏品名称 */
    private String name;

    /** 藏品分类 */
    private String category;

    /** 年代 */
    private String era;

    /** 材质 */
    private String material;

    /** 尺寸 */
    private String dimensions;

    /** 重量 */
    private String weight;

    /** 收藏状态：1=正常 2=借出 3=修复中 4=展出 */
    private Integer status;

    /** 存放位置（库房ID） */
    private Long storageId;

    /** 藏品描述 */
    private String description;

    /** 藏品图片 */
    private String imageUrl;

    /** 登记人 */
    private Long creatorId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 状态：1=正常 0=禁用 */
    private Integer status2;

    /** 备注 */
    private String remark;
}
