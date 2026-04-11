package com.jujing.museum.modules.collection.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 藏品DTO
 */
@Data
public class CollectionDTO {

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

    /** 库房名称 */
    private String storageName;

    /** 藏品描述 */
    private String description;

    /** 藏品图片 */
    private String imageUrl;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 创建人 */
    private String creatorName;
}
