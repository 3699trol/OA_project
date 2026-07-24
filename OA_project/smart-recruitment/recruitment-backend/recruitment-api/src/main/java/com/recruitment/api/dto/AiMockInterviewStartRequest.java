package com.recruitment.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI模拟面试-开始面试请求
 */
@Data
public class AiMockInterviewStartRequest {
    /** 职位ID */
    @NotNull(message = "目标职位不能为空")
    @Positive(message = "目标职位ID必须大于0")
    private Long jobId;
    /** 职位名称 */
    @NotBlank(message = "职位名称不能为空")
    @Size(max = 100, message = "职位名称不能超过100个字符")
    private String jobTitle;
    /** 任职要求 */
    private String jobRequirements;
    /** 岗位描述 */
    private String jobDescription;
    /** 候选人简历数据 */
    private Object resumeData;
    /** 面试类型：technical / hr / comprehensive */
    @Pattern(regexp = "^(technical|hr|comprehensive)$", message = "不支持的模拟面试类型")
    private String type;
    /** 难度：easy / medium / hard */
    @Pattern(regexp = "^(easy|medium|hard)$", message = "不支持的模拟面试难度")
    private String difficulty;
    /** 计划题目数量 */
    @Min(value = 3, message = "模拟面试题目数量不能小于3")
    @Max(value = 10, message = "模拟面试题目数量不能大于10")
    private Integer count;
}
