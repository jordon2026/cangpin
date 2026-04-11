package com.jujing.museum.modules.collection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * RFID设备实体
 */
@Data
@TableName("rfid_device")
public class RfidDevice {

    @TableId(type = IdType.AUTO)
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

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;
}
