package com.jujing.museum.modules.backup.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.jujing.museum.modules.backup.dto.BackupFileDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据备份服务
 * 使用 TCP 直连方式执行 mysqldump（不需要 docker exec）
 */
@Slf4j
@Service
public class BackupService {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${app.backup.path:/data/backup}")
    private String backupPath;

    @Value("${app.backup.mysqldump:}")
    private String mysqldumpPath;

    @Value("${app.backup.max-keep:30}")
    private int maxKeep;

    @Value("${app.backup.docker-container:}")
    private String dockerContainer;

    /**
     * 执行数据库备份
     * 使用 TCP 直连方式：直接连接 MySQL 执行 mysqldump
     */
    public BackupFileDTO backup() throws Exception {
        // 解析数据库连接信息
        String[] dbInfo = parseDbUrl(dbUrl);
        String host = dbInfo[0];
        int port = Integer.parseInt(dbInfo[1]);
        String dbName = dbInfo[2];

        // 创建备份目录
        File backupDir = new File(backupPath);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        // 生成备份文件名：museum_db_20260409_143055.sql
        String timestamp = DateUtil.format(new java.util.Date(), "yyyyMMdd_HHmmss");
        String fileName = String.format("%s_%s.sql", dbName, timestamp);
        String filePath = backupPath + File.separator + fileName;

        // 查找 mysqldump 命令
        String mysqldump = findMysqldump();

        // 构建命令
        List<String> command = new ArrayList<>();
        command.add(mysqldump);
        command.add("-h" + host);
        command.add("-P" + port);
        command.add("-u" + dbUsername);
        if (StrUtil.isNotEmpty(dbPassword)) {
            command.add("-p" + dbPassword);
        }
        command.add("--single-transaction");
        command.add("--quick");
        command.add("--triggers");
        command.add("--routines");
        command.add("--events");
        command.add(dbName);

        log.info("执行备份命令: {}", String.join(" ", command).replace("-p" + dbPassword, "-p******"));

        // 执行备份
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().put("MYSQL_PWD", StrUtil.isNotEmpty(dbPassword) ? dbPassword : "");
        pb.redirectErrorStream(false);

        Process process = pb.start();

        // 读取输出
        try (FileOutputStream fos = new FileOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            InputStream is = process.getInputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.flush();
        }

        // 等待命令完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            // 读取错误信息
            String errorMsg;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                errorMsg = reader.lines().collect(Collectors.joining("\n"));
            }
            log.error("备份失败: {}", errorMsg);
            throw new RuntimeException("备份失败: " + errorMsg);
        }

        // 检查文件是否生成
        File backupFile = new File(filePath);
        if (!backupFile.exists() || backupFile.length() == 0) {
            throw new RuntimeException("备份文件生成失败或文件为空");
        }

        // 清理旧备份
        cleanOldBackups();

        // 返回备份文件信息
        BackupFileDTO result = new BackupFileDTO();
        result.setFileName(fileName);
        result.setFilePath(filePath);
        result.setFileSize(backupFile.length());
        result.setSizeDesc(formatFileSize(backupFile.length()));
        result.setBackupTime(DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"));
        result.setCanRestore(true);

        log.info("备份成功: {}", filePath);
        return result;
    }

    /**
     * 获取备份文件列表
     */
    public List<BackupFileDTO> listBackups() {
        File backupDir = new File(backupPath);
        if (!backupDir.exists()) {
            return new ArrayList<>();
        }

        // 查找所有.sql文件并按时间倒序
        List<File> files = Arrays.stream(backupDir.listFiles((dir, name) -> name.endsWith(".sql")))
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .collect(Collectors.toList());

        return files.stream().map(file -> {
            BackupFileDTO dto = new BackupFileDTO();
            dto.setFileName(file.getName());
            dto.setFilePath(file.getAbsolutePath());
            dto.setFileSize(file.length());
            dto.setSizeDesc(formatFileSize(file.length()));
            dto.setBackupTime(DateUtil.formatDateTime(new java.util.Date(file.lastModified())));
            dto.setCanRestore(true);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 下载备份文件
     */
    public Resource downloadBackup(String fileName) {
        String filePath = backupPath + File.separator + fileName;
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("备份文件不存在: " + fileName);
        }
        return new FileSystemResource(file);
    }

    /**
     * 删除备份文件
     */
    public void deleteBackup(String fileName) {
        String filePath = backupPath + File.separator + fileName;
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("备份文件不存在: " + fileName);
        }
        if (!file.delete()) {
            throw new RuntimeException("删除备份文件失败: " + fileName);
        }
        log.info("删除备份文件: {}", filePath);
    }

    /**
     * 恢复数据库
     */
    public void restore(String fileName, InputStream inputStream) throws Exception {
        // 解析数据库连接信息
        String[] dbInfo = parseDbUrl(dbUrl);
        String host = dbInfo[0];
        int port = Integer.parseInt(dbInfo[1]);
        String dbName = dbInfo[2];

        String filePath;
        if (StrUtil.isNotEmpty(fileName) && inputStream == null) {
            // 从服务器文件恢复
            filePath = backupPath + File.separator + fileName;
            if (!new File(filePath).exists()) {
                throw new RuntimeException("备份文件不存在: " + fileName);
            }
        } else {
            // 上传文件恢复，保存到临时文件
            if (inputStream == null) {
                throw new RuntimeException("未提供备份文件");
            }
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            filePath = tempDir.getAbsolutePath() + File.separator + "restore_" + System.currentTimeMillis() + ".sql";
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            }
        }

        // 查找 mysql 命令
        String mysql = findMysql();

        // 构建命令 - 直接 TCP 连接
        List<String> command = new ArrayList<>();
        command.add(mysql);
        command.add("-h" + host);
        command.add("-P" + port);
        command.add("-u" + dbUsername);
        if (StrUtil.isNotEmpty(dbPassword)) {
            command.add("-p" + dbPassword);
        }
        command.add(dbName);

        log.info("执行恢复命令: {}", String.join(" ", command).replace("-p" + dbPassword, "-p******"));

        // 执行恢复
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().put("MYSQL_PWD", StrUtil.isNotEmpty(dbPassword) ? dbPassword : "");
        pb.redirectErrorStream(false);

        Process process = pb.start();

        // 写入SQL内容
        try (FileInputStream fis = new FileInputStream(filePath)) {
            OutputStream os = process.getOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.close();
        }

        // 等待命令完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String errorMsg;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                errorMsg = reader.lines().collect(Collectors.joining("\n"));
            }
            log.error("恢复失败: {}", errorMsg);
            throw new RuntimeException("恢复失败: " + errorMsg);
        }

        // 清理临时文件
        if (inputStream != null) {
            new File(filePath).delete();
        }

        log.info("数据库恢复成功");
    }

    /**
     * 清理旧备份文件
     */
    private void cleanOldBackups() {
        File backupDir = new File(backupPath);
        if (!backupDir.exists()) {
            return;
        }

        List<File> files = Arrays.stream(backupDir.listFiles((dir, name) -> name.endsWith(".sql")))
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .collect(Collectors.toList());

        // 保留最新的 maxKeep 个文件
        if (files.size() > maxKeep) {
            for (int i = maxKeep; i < files.size(); i++) {
                File file = files.get(i);
                if (file.delete()) {
                    log.info("清理旧备份文件: {}", file.getName());
                }
            }
        }
    }

    /**
     * 查找 mysqldump 命令
     */
    private String findMysqldump() {
        if (StrUtil.isNotEmpty(mysqldumpPath) && new File(mysqldumpPath).exists()) {
            return mysqldumpPath;
        }

        // 常见路径
        String[] paths = {
            "mysqldump",
            "mysql\\mysqldump.exe",
            "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
            "/usr/bin/mysqldump",
            "/usr/local/bin/mysqldump",
            "/usr/local/mysql/bin/mysqldump"
        };

        for (String path : paths) {
            if (new File(path).exists()) {
                return path;
            }
        }

        // 返回默认命令，让系统 PATH 处理
        return "mysqldump";
    }

    /**
     * 查找 mysql 命令
     */
    private String findMysql() {
        String[] paths = {
            "mysql",
            "mysql\\mysql.exe",
            "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe",
            "/usr/bin/mysql",
            "/usr/local/bin/mysql",
            "/usr/local/mysql/bin/mysql"
        };

        for (String path : paths) {
            if (new File(path).exists()) {
                return path;
            }
        }

        return "mysql";
    }

    /**
     * 解析数据库 URL
     */
    private String[] parseDbUrl(String url) {
        // jdbc:mysql://localhost:3306/museum_db?...
        // 支持格式: jdbc:mysql://host:port/dbname?params
        try {
            String withoutPrefix = url.replace("jdbc:mysql://", "");
            String[] withParams = withoutPrefix.split("\\?", 2);
            String hostPortDb = withParams[0];

            // 分割 host:port 和 dbname
            int slashIndex = hostPortDb.indexOf('/');
            if (slashIndex < 0) {
                throw new RuntimeException("无效的数据库URL，缺少数据库名: " + url);
            }

            String hostPort = hostPortDb.substring(0, slashIndex);
            String dbName = hostPortDb.substring(slashIndex + 1);

            // 分割 host 和 port
            String host;
            String port;
            if (hostPort.contains(":")) {
                int colonIndex = hostPort.lastIndexOf(':');
                host = hostPort.substring(0, colonIndex);
                port = hostPort.substring(colonIndex + 1);
            } else {
                host = hostPort;
                port = "3306";
            }

            return new String[]{host, port, dbName};
        } catch (Exception e) {
            log.error("解析数据库URL失败: {}", url, e);
            throw new RuntimeException("解析数据库URL失败: " + url, e);
        }
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
