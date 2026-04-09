package com.jujing.museum.modules.auth.dto;

import lombok.Data;
import java.util.List;

/**
 * 登录成功后返回的用户信息
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String avatar;
    private String token;
    private List<String> roles;
    private List<String> permissions;
}
