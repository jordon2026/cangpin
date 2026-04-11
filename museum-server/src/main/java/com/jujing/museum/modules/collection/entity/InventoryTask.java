package com.jujing.museum.modules.collection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 盘点任务实体
 */
@Data
@TableName("inventory_task")
public class InventoryTask {

    @TableId(type = IdType.AUTO)
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

    /** 实际开始时间 */
    private LocalDateTime startTime;

    /** 实际结束时间 */
    private LocalDateTime endTime;

    /** 任务状态: 0-待盘点, 1-盘点中, 2-已完成, 3-已取消 */
    private Integer status;

    /** 计划盘点数量 */
    private Integer planCount;

    /** 实际盘点数量 */
    private Integer actualCount;

    /** 盘点结果: 0-正常, 1-有差异 */
    private Integer result;

    /** 差异数量 */
    private Integer diffCount;

    /** 负责人ID */
    private Long handlerId;

    /** 负责人姓名 */
    private String handlerName;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
