package com.jujing.museum.modules.collection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 修复工单实体
 */
@Data
@TableName("repair_order")
public class RepairOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工单编号 */
    private String orderNo;

    /** 关联藏品ID */
    private Long collectionId;

    /** 关联藏品名称 */
    private String collectionName;

    /** 藏品编号 */
    private String collectionNo;

    /** 损坏描述 */
    private String damageDesc;

    /** 修复方案 */
    private String repairPlan;

    /** 修复费用 */
    private java.math.BigDecimal cost;

    /** 修复人 */
    private String repairer;

    /** 修复单位 */
    private String repairUnit;

    /** 工单状态: 0-待派单, 1-修复中, 2-已完成, 3-已取消 */
    private Integer status;

    /** 预计完成日期 */
    private LocalDateTime expectDate;

    /** 实际完成日期 */
    private LocalDateTime completeDate;

    /** 提交人ID */
    private Long submitterId;

    /** 提交人姓名 */
    private String submitterName;

    /** 处理人ID */
    private Long handlerId;

    /** 处理人姓名 */
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
