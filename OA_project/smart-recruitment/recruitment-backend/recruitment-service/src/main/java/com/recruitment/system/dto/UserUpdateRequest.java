package com.recruitment.system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名不能超过50个字符")
    private String realName;

    @NotNull(message = "用户角色不能为空")
    @Min(value = 1, message = "用户角色必须为1到4")
    @Max(value = 4, message = "用户角色必须为1到4")
    private Integer userType;

    @Pattern(regexp = "^(?:|1[3-9]\\d{9})$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String email;

    @NotNull(message = "账号状态不能为空")
    @Min(value = 0, message = "账号状态必须为0或1")
    @Max(value = 1, message = "账号状态必须为0或1")
    private Integer status;
}
