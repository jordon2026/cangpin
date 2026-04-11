package com.jujing.museum.modules.collection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 外借申请实体
 */
@Data
@TableName("loan_apply")
public class LoanApply {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请编号 */
    private String applyNo;

    /** 关联藏品ID */
    private Long collectionId;

    /** 关联藏品名称 */
    private String collectionName;

    /** 藏品编号 */
    private String collectionNo;

    /** 借入单位 */
    private String borrowerOrg;

    /** 借入单位联系人 */
    private String borrowerContact;

    /** 借入单位电话 */
    private String borrowerPhone;

    /** 借出用途 */
    private String purpose;

    /** 借出开始日期 */
    private LocalDateTime startDate;

    /** 借出结束日期 */
    private LocalDateTime endDate;

    /** 申请状态: 0-待审批, 1-已通过, 2-已拒绝, 3-已撤回 */
    private Integer status;

    /** 审批意见 */
    private String approvalOpinion;

    /** 审批人ID */
    private Long approverId;

    /** 审批人姓名 */
    private String approverName;

    /** 审批时间 */
    private LocalDateTime approvalTime;

    /** 是否已归还: 0-未归还, 1-已归还 */
    private Integer returned;

    /** 实际归还时间 */
    private LocalDateTime returnTime;

    /** 归还人 */
    private String returner;

    /** 归还备注 */
    private String returnRemark;

    /** 申请备注 */
    private String remark;

    /** 申请人ID */
    private Long applicantId;

    /** 申请人姓名 */
    private String applicantName;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
