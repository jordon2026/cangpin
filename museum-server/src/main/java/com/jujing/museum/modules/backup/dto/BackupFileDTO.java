package com.jujing.museum.modules.backup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 备份文件信息DTO
 */
@Data
@Schema(description = "备份文件信息")
public class BackupFileDTO {

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "备份时间")
    private String backupTime;

    @Schema(description = "文件大小描述")
    private String sizeDesc;

    @Schema(description = "是否可以恢复")
    private Boolean canRestore;
}
