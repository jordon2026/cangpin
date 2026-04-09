package com.jujing.museum.aspect;

import com.alibaba.fastjson2.JSON;
import com.jujing.museum.annotation.OperLog;
import com.jujing.museum.modules.auth.entity.SysOperLog;
import com.jujing.museum.modules.auth.mapper.SysOperLogMapper;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final SysOperLogMapper operLogMapper;

    @Pointcut("@annotation(operLog)")
    public void operLogPointCut(OperLog operLog) {
    }

    @Around("operLogPointCut(operLog)")
    public Object around(ProceedingJoinPoint point, OperLog operLog) throws Throwable {
        SysOperLog sysOperLog = new SysOperLog();
        sysOperLog.setTitle(operLog.title());
        sysOperLog.setBusinessType(operLog.type());

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                sysOperLog.setOperUrl(request.getRequestURI());
                sysOperLog.setRequestMethod(request.getMethod());
                sysOperLog.setIp(getIpAddr(request));

                // 获取当前登录用户
                try {
                    sysOperLog.setOperUser(StpUtil.getLoginIdAsString());
                } catch (Exception ignored) {
                    // 未登录不记录操作人
                }

                // 记录请求参数（过滤敏感字段）
                MethodSignature signature = (MethodSignature) point.getSignature();
                String[] paramNames = signature.getParameterNames();
                Object[] args = point.getArgs();
                StringBuilder params = new StringBuilder("{");
                if (paramNames != null && args != null) {
                    boolean first = true;
                    for (int i = 0; i < paramNames.length; i++) {
                        // 排除 request 和 response 对象
                        if (args[i] instanceof HttpServletRequest || args[i] instanceof HttpServletResponse) {
                            continue;
                        }
                        if (!first) {
                            params.append(",");
                        }
                        first = false;
                        params.append(paramNames[i]).append(":");
                        try {
                            String json = JSON.toJSONString(args[i]);
                            // 【安全修复】脱敏处理：替换敏感字段值
                            json = desensitize(json);
                            // 限制参数长度防止过大
                            if (json.length() > 2000) {
                                json = json.substring(0, 2000) + "...";
                            }
                            params.append(json);
                        } catch (Exception e) {
                            params.append("[记录失败]");
                        }
                    }
                }
                params.append("}");
                sysOperLog.setRequestParams(params.toString());
            }

            sysOperLog.setMethod(point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName());

            // 执行方法
            Object result = point.proceed();
            sysOperLog.setStatus(1);
            try {
                String resultJson = JSON.toJSONString(result);
                if (resultJson.length() > 2000) {
                    resultJson = resultJson.substring(0, 2000) + "...";
                }
                sysOperLog.setResponseResult(resultJson);
            } catch (Exception ignored) {
            }
            return result;
        } catch (Throwable e) {
            sysOperLog.setStatus(0);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 2000) {
                errorMsg = errorMsg.substring(0, 2000);
            }
            sysOperLog.setErrorMsg(errorMsg);
            throw e;
        } finally {
            sysOperLog.setOperTime(LocalDateTime.now());
            try {
                operLogMapper.insert(sysOperLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            ip = ip.split(",")[0].trim();
        } else {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * 日志脱敏：将 JSON 字符串中的敏感字段值替换为 "***"
     */
    private static final java.util.regex.Pattern SENSITIVE_PATTERN = java.util.regex.Pattern.compile(
            "\"(password|passwd|pwd|newPassword|oldPassword|confirmPassword|token|accessToken|refreshToken|secret|secretKey|privateKey|idCard|bankCard|cardNo)\"\\s*:\\s*\"[^\"]*\"",
            java.util.regex.Pattern.CASE_INSENSITIVE
    );

    private String desensitize(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        return SENSITIVE_PATTERN.matcher(json).replaceAll(m -> {
            String key = m.group(1);
            return "\"" + key + "\":\"***\"";
        });
    }
}
