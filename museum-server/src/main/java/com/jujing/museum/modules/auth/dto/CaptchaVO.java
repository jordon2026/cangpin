package com.jujing.museum.modules.auth.dto;

import lombok.Data;

/**
 * 验证码响应
 */
@Data
public class CaptchaVO {

    /** 验证码唯一标识 */
    private String uuid;

    /** 验证码图片（Base64） */
    private String img;
}
