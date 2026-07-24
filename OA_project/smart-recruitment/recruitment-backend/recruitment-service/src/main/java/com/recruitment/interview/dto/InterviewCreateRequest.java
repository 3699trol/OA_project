package com.recruitment.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InterviewCreateRequest {

    @NotNull(message = "候选人不能为空")
    @Positive(message = "候选人ID必须大于0")
    private Long candidateId;

    @NotNull(message = "投递记录不能为空")
    @Positive(message = "投递记录ID必须大于0")
    private Long applicationId;

    @NotNull(message = "面试官不能为空")
    @Positive(message = "面试官ID必须大于0")
    private Long interviewerId;

    @NotBlank(message = "面试类型不能为空")
    @Pattern(regexp = "^(技术|HR|综合)$", message = "不支持的面试类型")
    private String type;

    @NotBlank(message = "面试方式不能为空")
    @Pattern(regexp = "^(线上|线下)$", message = "不支持的面试方式")
    private String mode;

    @NotBlank(message = "面试时间不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$",
            message = "面试时间格式应为yyyy-MM-dd HH:mm")
    private String interviewTime;

    @Size(max = 255, message = "面试地点或链接不能超过255个字符")
    private String address;
}
