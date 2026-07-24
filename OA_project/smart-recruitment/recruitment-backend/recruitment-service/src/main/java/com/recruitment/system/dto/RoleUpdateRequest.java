package com.recruitment.system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleUpdateRequest {

    @Pattern(regexp = "^[A-Z_]+$", message = "角色编码必须为大写字母和下划线")
    @Size(max = 50, message = "角色编码不能超过50个字符")
    private String roleCode;

    @Pattern(regexp = ".*\\S.*", message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称不能超过50个字符")
    private String roleName;

    @Size(max = 255, message = "角色描述不能超过255个字符")
    private String description;

    @Min(value = 0, message = "角色状态必须为0或1")
    @Max(value = 1, message = "角色状态必须为0或1")
    private Integer status;
}
