package com.jujing.museum.modules.collection.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库房DTO
 */
@Data
public class StorageDTO {

    private Long id;

    /** 库房编号 */
    private String storageNo;

    /** 库房名称 */
    private String name;

    /** 位置 */
    private String location;

    /** 面积(㎡) */
    private BigDecimal area;

    /** 安全等级：1=一级 2=二级 3=三级 */
    private Integer safetyLevel;

    /** 容量 */
    private Integer capacity;

    /** 当前藏品数 */
    private Integer currentCount;

    /** 负责人 */
    private String manager;

    /** 联系电话 */
    private String phone;

    /** 温度(℃) */
    private BigDecimal temperature;

    /** 湿度(%) */
    private BigDecimal humidity;

    /** 状态：1=正常 0=禁用 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;
}
