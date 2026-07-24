package com.recruitment.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InterviewQuestionGenerateRequest {

    @Positive(message = "职位ID必须大于0")
    private Long jobId;

    @Size(max = 100, message = "职位名称不能超过100个字符")
    private String jobTitle;

    private String jobDescription;
    private String resumeContent;

    @Pattern(regexp = "^(基础理论|项目经验|系统设计|算法编程|行为面试)$",
            message = "不支持的面试题型")
    private String questionType;

    @Pattern(regexp = "^(简单|中等|困难)$", message = "不支持的面试题难度")
    private String difficulty;

    @Min(value = 1, message = "面试题数量不能小于1")
    @Max(value = 20, message = "面试题数量不能大于20")
    private Integer count;
}
