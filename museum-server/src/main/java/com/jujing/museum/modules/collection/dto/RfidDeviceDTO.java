package com.jujing.museum.modules.collection.dto;

import lombok.Data;

/**
 * RFID设备DTO
 */
@Data
public class RfidDeviceDTO {

    private Long id;

    /** 设备名称 */
    private String name;

    /** 设备型号 */
    private String model;

    /** 设备编号 */
    private String deviceNo;

    /** 设备IP */
    private String ip;

    /** 设备端口 */
    private Integer port;

    /** 设备状态: 0-离线, 1-在线, 2-维护中 */
    private Integer status;

    /** 关联库房ID */
    private Long storageId;

    /** 关联库房名称 */
    private String storageName;

    /** 备注 */
    private String remark;
}
