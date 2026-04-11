package com.jujing.museum.modules.collection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 盘点记录实体
 */
@Data
@TableName("inventory_record")
public class InventoryRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联任务ID */
    private Long taskId;

    /** 关联任务编号 */
    private String taskNo;

    /** 关联藏品ID */
    private Long collectionId;

    /** 关联藏品名称 */
    private String collectionName;

    /** 藏品编号 */
    private String collectionNo;

    /** RFID标签 */
    private String rfidTag;

    /** 盘点状态: 0-未盘点, 1-已盘点, 2-盘亏(未找到) */
    private Integer status;

    /** 盘点时间 */
    private LocalDateTime scanTime;

    /** 盘点人ID */
    private Long operatorId;

    /** 盘点人姓名 */
    private String operatorName;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
