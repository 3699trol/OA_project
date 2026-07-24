package com.recruitment.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class InterviewEvaluationRequest {

    @NotNull(message = "面试ID不能为空")
    @Positive(message = "面试ID必须大于0")
    private Long interviewId;

    @NotNull(message = "技术评分不能为空")
    @Min(value = 0, message = "技术评分不能小于0")
    @Max(value = 100, message = "技术评分不能大于100")
    private Integer technicalScore;

    @NotNull(message = "沟通评分不能为空")
    @Min(value = 0, message = "沟通评分不能小于0")
    @Max(value = 100, message = "沟通评分不能大于100")
    private Integer communicationScore;

    @NotNull(message = "逻辑评分不能为空")
    @Min(value = 0, message = "逻辑评分不能小于0")
    @Max(value = 100, message = "逻辑评分不能大于100")
    private Integer logicScore;

    @NotNull(message = "综合评分不能为空")
    @Min(value = 0, message = "综合评分不能小于0")
    @Max(value = 100, message = "综合评分不能大于100")
    private Integer overallScore;

    @NotNull(message = "面试结果不能为空")
    @Min(value = 0, message = "面试结果必须为0到2")
    @Max(value = 2, message = "面试结果必须为0到2")
    private Integer result;

    private String evaluation;
}
