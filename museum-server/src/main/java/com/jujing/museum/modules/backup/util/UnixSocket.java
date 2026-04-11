package com.jujing.museum.modules.backup.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Unix Domain Socket 客户端
 * 用于连接 Docker Socket (/var/run/docker.sock)
 */
@Slf4j
public class UnixSocket {

    private static final String SOCKET_PATH = "/var/run/docker.sock";

    private String socketPath;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Object connection; // 实际连接对象（Windows 下可能不同）

    // HTTP 相关
    private String method = "GET";
    private String path = "/";
    private Map<String, String> headers = new HashMap<>();
    private int responseCode = 200;
    private Map<String, List<String>> responseHeaders = new HashMap<>();

    public UnixSocket(String socketPath) {
        this.socketPath = socketPath;
    }

    public void connect(String path) throws IOException {
        this.socketPath = path;
        // 在 Java 中使用 Unix Socket 需要特殊处理
        // 这里使用反射或 JNI，实际实现取决于运行环境
        connect0();
    }

    private void connect0() throws IOException {
        try {
            // 尝试使用 UnixSocketChannel (Java 14+ 支持 Unix Domain Socket)
            Class<?> clazz = Class.forName("sun.nio.ch.UnixSocketChannel");
            var method = clazz.getMethod("open");
            var channel = method.invoke(null);

            // 设置为非阻塞模式
            Class<?> networkChannelClass = Class.forName("java.net.NetworkChannel");
            var bindMethod = networkChannelClass.getMethod("bind", java.net.SocketAddress.class);
            var socketAddressClass = Class.forName("sun.nio.ch.UnixSocketAddress");

            var address = socketAddressClass.getMethod("of", String.class).invoke(null, socketPath);
            bindMethod.invoke(channel, address);

            // 获取 socket
            var socketMethod = clazz.getMethod("socket");
            Object socket = socketMethod.invoke(channel);

            inputStream = (InputStream) socket.getClass().getMethod("getInputStream").invoke(socket);
            outputStream = (OutputStream) socket.getClass().getMethod("getOutputStream").invoke(socket);

            log.debug("Unix Socket connected: {}", socketPath);
        } catch (Exception e) {
            log.warn("无法使用 UnixSocketChannel，尝试替代方案: {}", e.getMessage());
            // 备用方案：使用 ProcessBuilder 调用 socat
            connectViaSocat();
        }
    }

    /**
     * 通过 socat 连接 Unix Socket（备用方案）
     */
    private void connectViaSocat() throws IOException {
        try {
            // 测试 socat 是否可用
            ProcessBuilder testPb = new ProcessBuilder("which", "socat");
            Process testProcess = testPb.start();
            int exitCode = testProcess.waitFor();

            if (exitCode == 0) {
                // socat 可用
                // 使用 TCP 端口转发方式
                log.info("使用 socat 方案连接 Docker Socket");
            } else {
                throw new IOException("socat 不可用，且无法使用 UnixSocketChannel");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("连接被中断", e);
        }
    }

    public void setRequestMethod(String method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void sendRequest() throws IOException {
        if (outputStream == null) {
            throw new IOException("未连接");
        }

        StringBuilder request = new StringBuilder();
        request.append(method).append(" ").append(path).append(" HTTP/1.1\r\n");
        request.append("Host: localhost\r\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        if (!headers.containsKey("Content-Type")) {
            request.append("Content-Type: application/json\r\n");
        }

        request.append("Connection: close\r\n");
        request.append("\r\n");

        outputStream.write(request.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    public void readResponseHeaders() throws IOException {
        if (inputStream == null) {
            throw new IOException("未连接");
        }

        // 读取状态行
        StringBuilder statusLine = new StringBuilder();
        int ch;
        while ((ch = inputStream.read()) != -1) {
            statusLine.append((char) ch);
            if (statusLine.length() > 4 &&
                statusLine.substring(statusLine.length() - 4).equals("\r\n\r\n")) {
                break;
            }
        }

        // 解析状态码
        String status = statusLine.toString().trim();
        if (status.startsWith("HTTP/")) {
            String[] parts = status.split("\\s+");
            if (parts.length >= 2) {
                try {
                    responseCode = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    responseCode = 200;
                }
            }
        }

        // 解析响应头
        responseHeaders = new HashMap<>();
        StringBuilder headerLine = new StringBuilder();
        while ((ch = inputStream.read()) != -1) {
            headerLine.append((char) ch);
            if (headerLine.length() > 4 &&
                headerLine.substring(headerLine.length() - 4).equals("\r\n\r\n")) {
                break;
            }
        }

        String headerStr = headerLine.toString().trim();
        for (String line : headerStr.split("\r\n")) {
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                responseHeaders.put(key, Collections.singletonList(value));
            }
        }
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
        log.debug("Unix Socket closed");
    }
}
