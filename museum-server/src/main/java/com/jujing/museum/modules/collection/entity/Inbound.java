package com.jujing.museum.modules.collection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入库记录表
 */
@Data
@TableName("inbound")
public class Inbound {

    @TableId(type = IdType.AUTO)
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

    /** 经手人 */
    private String handler;

    /** 入库日期 */
    private LocalDate inboundDate;

    /** 状态：0=待审核 1=已入库 2=已拒绝 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 申请人ID */
    private Long applicantId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
