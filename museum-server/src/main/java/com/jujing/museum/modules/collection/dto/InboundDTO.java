package com.jujing.museum.modules.collection.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入库DTO
 */
@Data
public class InboundDTO {

    private Long id;

    /** 入库单号 */
    private String inboundNo;

    /** 藏品ID */
    private Long collectionId;

    /** 藏品名称 */
    private String collectionName;

    /** 藏品编号 */
    private String collectionNo;

    /** 数量 */
    private Integer quantity;

    /** 来源 */
    private String source;

    /** 入库库房ID */
    private Long storageId;

    /** 库房名称 */
    private String storageName;

    /** 经手人 */
    private String handler;

    /** 入库日期 */
    private LocalDate inboundDate;

    /** 状态：0=待审核 1=已入库 2=已拒绝 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 申请人 */
    private String applicantName;
}
