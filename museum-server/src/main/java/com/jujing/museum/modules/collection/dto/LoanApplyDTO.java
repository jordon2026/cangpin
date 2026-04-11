package com.jujing.museum.modules.collection.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 外借申请DTO
 */
@Data
public class LoanApplyDTO {

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

    /** 归还备注 */
    private String returnRemark;

    /** 申请备注 */
    private String remark;
}
