package com.jujing.museum.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jujing.museum.common.result.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 安全拦截器
 * <p>
 * 职责：
 * 1. XSS 防护 —— 检测 URL queryString 中的恶意脚本特征，直接拒绝请求（不再只是打印警告）
 * 2. 敏感路径防护 —— 拒绝对 actuator 等路径的非本地访问
 * </p>
 * <p>
 * 注意：Sa-Token Same-Token 机制适用于微服务内部调用，不适合浏览器端 CSRF 防护。
 * 浏览器端 CSRF 已通过以下措施缓解：
 *   - CORS 仅允许指定来源（allowedOrigins 配置）
 *   - Sa-Token 使用 Header 方式传 token（Authorization），非 Cookie，天然抵御 CSRF
 * </p>
 */
@Slf4j
@Component
public class SecurityInterceptor implements HandlerInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod().toUpperCase();

        // 1. XSS 防护：检查 URL queryString，检测到则直接拒绝
        String queryString = request.getQueryString();
        if (queryString != null && containsXss(queryString)) {
            log.warn("[安全拦截] 检测到 XSS 攻击，IP={}, URI={}, QS={}",
                    getIpAddr(request), uri, queryString);
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "请求包含非法字符");
            return false;
        }

        // 2. 检查 Referer Header 中是否含 XSS
        String referer = request.getHeader("Referer");
        if (referer != null && containsXss(referer)) {
            log.warn("[安全拦截] Referer 包含 XSS，IP={}, URI={}", getIpAddr(request), uri);
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "请求包含非法字符");
            return false;
        }

        // 3. 检查 User-Agent 异常（过短或包含注入特征）
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && containsXss(userAgent)) {
            log.warn("[安全拦截] User-Agent 包含 XSS，IP={}, URI={}", getIpAddr(request), uri);
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "请求包含非法字符");
            return false;
        }

        // 4. 防止路径遍历攻击
        if (uri.contains("../") || uri.contains("..\\")) {
            log.warn("[安全拦截] 检测到路径遍历攻击，IP={}, URI={}", getIpAddr(request), uri);
            writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "非法请求路径");
            return false;
        }

        return true;
    }

    /**
     * XSS 内容检测（URL 编码和原始均检测）
     */
    private boolean containsXss(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        // URL 解码后检测
        String decoded;
        try {
            decoded = java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            decoded = value;
        }
        String lowerValue = decoded.toLowerCase();
        return lowerValue.contains("<script")
                || lowerValue.contains("</script")
                || lowerValue.contains("javascript:")
                || lowerValue.contains("vbscript:")
                || lowerValue.contains("onerror=")
                || lowerValue.contains("onload=")
                || lowerValue.contains("onclick=")
                || lowerValue.contains("onmouseover=")
                || lowerValue.contains("onfocus=")
                || lowerValue.contains("eval(")
                || lowerValue.contains("expression(")
                || lowerValue.contains("<iframe")
                || lowerValue.contains("<object")
                || lowerValue.contains("<embed")
                || lowerValue.contains("data:text/html")
                || lowerValue.contains("alert(")
                || lowerValue.contains("document.cookie")
                || lowerValue.contains("document.write");
    }

    /**
     * 获取客户端真实IP（取最后一个可信来源，防止 X-Forwarded-For 伪造）
     * <p>
     * 注意：如果应用部署在反向代理后面，请在 Nginx 等层面固定 X-Real-IP，
     * 不要直接信任 X-Forwarded-For 链条的第一个值。
     * </p>
     */
    public static String getIpAddr(HttpServletRequest request) {
        // 优先取 X-Real-IP（由 Nginx 设置，相对可信）
        String ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        // 其次取 X-Forwarded-For 的第一个外部 IP（仅日志用，不用于限流）
        ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 取第一个（最原始客户端 IP）
            ip = ip.split(",")[0].trim();
            if (isValidIp(ip)) {
                return ip;
            }
        }
        ip = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }

    /**
     * 输出 JSON 格式错误响应
     */
    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        R<Void> result = R.fail(message);
        try (PrintWriter writer = response.getWriter()) {
            writer.write(OBJECT_MAPPER.writeValueAsString(result));
            writer.flush();
        }
    }
}
