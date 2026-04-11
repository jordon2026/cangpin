package com.jujing.museum.modules.collection.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 盘点记录DTO
 */
@Data
public class InventoryRecordDTO {

    private Long id;

    /** 关联任务ID */
    private Long taskId;

    /** 关联藏品ID */
    private Long collectionId;

    /** 关联藏品名称 */
    private String collectionName;

    /** 藏品编号 */
    private String collectionNo;

    /** RFID标签 */
    private String rfidTag;

    /** 盘点状态: 0-未盘点, 1-已盘点, 2-盘亏 */
    private Integer status;

    /** 备注 */
    private String remark;
}
