package com.jujing.museum.modules.collection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * RFID标签实体
 */
@Data
@TableName("rfid_tag")
public class RfidTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签编码 */
    private String tagCode;

    /** 标签EPC */
    private String epc;

    /** 关联藏品ID */
    private Long collectionId;

    /** 关联藏品名称 */
    private String collectionName;

    /** 标签状态: 0-未绑定, 1-已绑定, 2-已挂失 */
    private Integer status;

    /** 绑定时间 */
    private LocalDateTime bindTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;
}
