package com.jujing.museum.modules.collection.dto;

import lombok.Data;

/**
 * RFID标签DTO
 */
@Data
public class RfidTagDTO {

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

    /** 备注 */
    private String remark;
}
