package com.recruitment.job.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobCategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称不能超过100个字符")
    private String name;

    @Size(max = 500, message = "分类描述不能超过500个字符")
    private String description;

    @Min(value = 0, message = "分类状态必须为0或1")
    @Max(value = 1, message = "分类状态必须为0或1")
    private Integer status;
}
