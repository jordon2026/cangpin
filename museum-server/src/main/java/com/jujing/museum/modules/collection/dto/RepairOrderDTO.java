package com.jujing.museum.modules.collection.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 修复工单DTO
 */
@Data
public class RepairOrderDTO {

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
    private BigDecimal cost;

    /** 修复人 */
    private String repairer;

    /** 修复单位 */
    private String repairUnit;

    /** 工单状态: 0-待派单, 1-修复中, 2-已完成, 3-已取消 */
    private Integer status;

    /** 预计完成日期 */
    private LocalDateTime expectDate;

    /** 备注 */
    private String remark;
}
