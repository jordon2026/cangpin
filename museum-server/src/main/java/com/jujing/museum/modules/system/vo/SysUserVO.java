package com.jujing.museum.modules.system.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户列表 VO（不含密码）
 */
@Data
public class SysUserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer status;
    private Long deptId;
    private String deptName;
    private List<Long> roleIds;
    private List<String> roleNames;
    private LocalDateTime createTime;
}
