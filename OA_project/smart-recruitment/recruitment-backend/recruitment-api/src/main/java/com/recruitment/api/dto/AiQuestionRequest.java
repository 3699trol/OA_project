package com.recruitment.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

/**
 * AI面试题生成请求
 */
@Data
public class AiQuestionRequest {
    @NotBlank(message = "职位名称不能为空")
    @Size(max = 100, message = "职位名称不能超过100个字符")
    private String jobTitle;

    @NotBlank(message = "职位描述不能为空")
    private String jobDescription;
    private String resumeContent;

    @Size(max = 50, message = "题型不能超过50个字符")
    private String questionType;

    @Min(value = 1, message = "面试题数量不能小于1")
    @Max(value = 20, message = "面试题数量不能大于20")
    private Integer count;

    @Size(max = 20, message = "难度不能超过20个字符")
    private String difficulty;
}
