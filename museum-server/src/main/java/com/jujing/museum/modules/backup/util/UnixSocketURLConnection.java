package com.jujing.museum.modules.backup.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * 通过 Unix Domain Socket 连接 HTTP 服务器
 * 用于访问 Docker Socket API
 */
public class UnixSocketURLConnection extends HttpURLConnection {

    private final UnixSocket socket;
    private final URL url;
    private final String socketPath;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Map<String, List<String>> responseHeaders;
    private int responseCode = -1;

    public UnixSocketURLConnection(URL url, String socketPath) throws IOException {
        super(url);
        this.url = url;
        this.socketPath = socketPath;
        this.socket = new UnixSocket(socketPath);
    }

    @Override
    public void connect() throws IOException {
        if (!connected) {
            socket.connect(socketPath);
            connected = true;
        }
    }

    @Override
    public void disconnect() {
        if (connected) {
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
            connected = false;
        }
    }

    @Override
    public boolean usingProxy() {
        return false;
    }

    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        try {
            if (!connected) {
                connect();
            }
        } catch (IOException e) {
            throw new ProtocolException("Failed to connect: " + e.getMessage());
        }
        socket.setRequestMethod(method);
        socket.setPath(url.getPath() + (url.getQuery() != null ? "?" + url.getQuery() : ""));
    }

    @Override
    public void setRequestProperty(String key, String value) {
        socket.addHeader(key, value);
    }

    @Override
    public void setDoOutput(boolean dooutput) {
        this.doOutput = dooutput;
    }

    @Override
    public void setDoInput(boolean doinput) {
        this.doInput = doinput;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (!connected) {
            connect();
        }
        if (outputStream == null) {
            outputStream = socket.getOutputStream();
        }
        return outputStream;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (!connected) {
            connect();
        }
        // 发送请求并获取响应
        socket.sendRequest();
        inputStream = socket.getInputStream();
        return inputStream;
    }

    @Override
    public InputStream getErrorStream() {
        return inputStream;
    }

    @Override
    public int getResponseCode() throws IOException {
        if (responseCode == -1) {
            // 读取响应头获取状态码
            socket.readResponseHeaders();
            responseCode = socket.getResponseCode();
            responseHeaders = socket.getResponseHeaders();
        }
        return responseCode;
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return responseHeaders;
    }
}
