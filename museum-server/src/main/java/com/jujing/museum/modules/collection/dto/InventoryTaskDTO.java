package com.jujing.museum.modules.collection.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 盘点任务DTO
 */
@Data
public class InventoryTaskDTO {

    private Long id;

    /** 任务编号 */
    private String taskNo;

    /** 任务名称 */
    private String name;

    /** 关联库房ID */
    private Long storageId;

    /** 关联库房名称 */
    private String storageName;

    /** 计划盘点日期 */
    private LocalDateTime planDate;

    /** 任务状态: 0-待盘点, 1-盘点中, 2-已完成, 3-已取消 */
    private Integer status;

    /** 负责人ID */
    private Long handlerId;

    /** 负责人姓名 */
    private String handlerName;

    /** 备注 */
    private String remark;
}
