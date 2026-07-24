package com.recruitment.job.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建职位请求
 */
@Data
public class JobCreateRequest {

    @NotBlank(message = "职位名称不能为空")
    @Size(max = 100, message = "职位名称不能超过100个字符")
    private String jobName;

    @Size(max = 100, message = "部门名称不能超过100个字符")
    private String department;

    @NotBlank(message = "职位类别不能为空")
    @Size(max = 50, message = "职位类别不能超过50个字符")
    private String category;

    @Size(max = 100, message = "工作城市不能超过100个字符")
    private String city;

    @DecimalMin(value = "0", message = "最低薪资不能小于0")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0", message = "最高薪资不能小于0")
    private BigDecimal salaryMax;

    @Size(max = 50, message = "学历要求不能超过50个字符")
    private String education;

    @Size(max = 50, message = "经验要求不能超过50个字符")
    private String experience;

    @NotNull(message = "招聘人数不能为空")
    @Min(value = 1, message = "招聘人数不能小于1")
    private Integer headcount;
    private String description;
    private String requirements;

    @Size(max = 500, message = "技能标签不能超过500个字符")
    private String skills; // 技能标签，逗号分隔
}
