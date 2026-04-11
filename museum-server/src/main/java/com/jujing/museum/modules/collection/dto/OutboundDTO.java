package com.jujing.museum.modules.collection.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 出库DTO
 */
@Data
public class OutboundDTO {

    private Long id;

    /** 出库单号 */
    private String outboundNo;

    /** 藏品ID */
    private Long collectionId;

    /** 藏品名称 */
    private String collectionName;

    /** 藏品编号 */
    private String collectionNo;

    /** 数量 */
    private Integer quantity;

    /** 目的地 */
    private String destination;

    /** 出库原因 */
    private String reason;

    /** 出库库房ID */
    private Long storageId;

    /** 库房名称 */
    private String storageName;

    /** 经手人 */
    private String handler;

    /** 出库日期 */
    private LocalDate outboundDate;

    /** 预计归还日期 */
    private LocalDate expectedReturnDate;

    /** 状态：0=待审核 1=已出库 2=已拒绝 3=已归还 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 申请人 */
    private String applicantName;
}
