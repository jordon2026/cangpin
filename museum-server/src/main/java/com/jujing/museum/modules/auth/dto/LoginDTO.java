package com.jujing.museum.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码UUID */
    @NotBlank(message = "验证码标识不能为空")
    private String uuid;

    /** 验证码答案 */
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}
