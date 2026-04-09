package com.jujing.museum.modules.system.vo;

import lombok.Data;

import java.util.List;

/**
 * 部门树形 VO
 */
@Data
public class DeptTreeVO {
    private Long id;
    private Long parentId;
    private String deptName;
    private Integer sort;
    private String leader;
    private String phone;
    private String email;
    private Integer status;
    private List<DeptTreeVO> children;
}
