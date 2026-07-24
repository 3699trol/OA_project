package com.recruitment.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名或邮箱不能为空")
    @Size(max = 100, message = "用户名或邮箱不能超过100个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度应为6到64位")
    private String password;

    private String expectedRole;
}
