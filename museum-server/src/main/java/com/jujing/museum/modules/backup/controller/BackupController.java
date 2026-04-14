package com.jujing.museum.modules.backup.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.backup.dto.BackupFileDTO;
import com.jujing.museum.modules.backup.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 数据备份管理
 */
@Slf4j
@Tag(name = "数据备份")
@RestController
@RequestMapping("/api/v1/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;

    @Operation(summary = "执行数据库备份")
    @SaCheckPermission("system:backup:create")
    @PostMapping("/create")
    public R<BackupFileDTO> createBackup() {
        try {
            BackupFileDTO result = backupService.backup();
            return R.ok("备份成功", result);
        } catch (Exception e) {
            log.error("备份失败", e);
            return R.fail("备份失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取备份文件列表")
    @SaCheckPermission("system:backup:list")
    @GetMapping("/list")
    public R<List<BackupFileDTO>> listBackups() {
        List<BackupFileDTO> backups = backupService.listBackups();
        return R.ok(backups);
    }

    @Operation(summary = "下载备份文件")
    @SaCheckPermission("system:backup:download")
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadBackup(
            @Parameter(description = "备份文件名") @RequestParam String fileName) {
        Resource resource = backupService.downloadBackup(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @Operation(summary = "上传并恢复备份")
    @SaCheckPermission("system:backup:restore")
    @PostMapping("/restore/upload")
    public R<Void> restoreFromUpload(
            @Parameter(description = "备份文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "确认恢复") @RequestParam(required = false, defaultValue = "false") Boolean confirmed) {
        if (!confirmed) {
            return R.fail("请确认恢复操作，防止误操作");
        }

        try {
            backupService.restore(null, file.getInputStream());
            return R.ok("恢复成功", null);
        } catch (Exception e) {
            log.error("恢复失败", e);
            return R.fail("恢复失败: " + e.getMessage());
        }
    }

    @Operation(summary = "从服务器文件恢复备份")
    @SaCheckPermission("system:backup:restore")
    @PostMapping("/restore/file")
    public R<Void> restoreFromFile(
            @Parameter(description = "备份文件名") @RequestParam String fileName,
            @Parameter(description = "确认恢复") @RequestParam(required = false, defaultValue = "false") Boolean confirmed) {
        if (!confirmed) {
            return R.fail("请确认恢复操作，防止误操作");
        }

        try {
            backupService.restore(fileName, null);
            return R.ok("恢复成功", null);
        } catch (Exception e) {
            log.error("恢复失败", e);
            return R.fail("恢复失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除备份文件")
    @SaCheckPermission("system:backup:delete")
    @DeleteMapping("/{fileName}")
    public R<Void> deleteBackup(
            @Parameter(description = "备份文件名") @PathVariable String fileName) {
        try {
            backupService.deleteBackup(fileName);
            return R.ok("删除成功", null);
        } catch (Exception e) {
            log.error("删除备份失败", e);
            return R.fail("删除失败: " + e.getMessage());
        }
    }
}
