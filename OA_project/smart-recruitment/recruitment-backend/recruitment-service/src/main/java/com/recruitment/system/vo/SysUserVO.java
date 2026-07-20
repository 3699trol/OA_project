package com.recruitment.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象（不含密码字段，禁止敏感信息泄露到前端）
 */
@Data
public class SysUserVO {

    private Long id;
    private String username;
    private String realName;
    private Integer userType;
    private Integer gender;
    private String phone;
    private String email;
    private String avatarUrl;
    private Long companyId;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
