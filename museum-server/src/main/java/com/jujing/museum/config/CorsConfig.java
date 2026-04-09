package com.jujing.museum.config;

/**
 * 跨域配置
 * <p>
 * CORS 统一在 {@link SaTokenConfig#addCorsMappings} 中配置（基于 allowedOrigins 属性）。
 * 本类保留占位，避免其他地方引用报错；不再重复注册 CorsFilter，
 * 防止"双重 CORS 响应头"导致浏览器报错。
 * </p>
 */
public class CorsConfig {
    // CORS 配置已迁移至 SaTokenConfig#addCorsMappings
    // 允许的来源通过 app.cors.allowed-origins 属性配置（见 application.yml）
}
