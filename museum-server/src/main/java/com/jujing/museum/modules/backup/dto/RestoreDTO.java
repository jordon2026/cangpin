package com.jujing.museum.modules.backup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 恢复备份参数DTO
 */
@Data
@Schema(description = "恢复备份参数")
public class RestoreDTO {

    @Schema(description = "备份文件路径（服务器已有文件恢复）")
    private String filePath;

    @Schema(description = "上传的备份文件（文件上传恢复）")
    private MultipartFile file;

    @Schema(description = "是否确认恢复操作（防止误操作）")
    private Boolean confirmed;
}
