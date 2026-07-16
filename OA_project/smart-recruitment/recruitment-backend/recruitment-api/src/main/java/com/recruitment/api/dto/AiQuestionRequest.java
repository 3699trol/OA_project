package com.recruitment.api.dto;

import lombok.Data;
import java.util.List;

/**
 * AI面试题生成请求
 */
@Data
public class AiQuestionRequest {
    private String jobTitle;
    private String jobDescription;
    private String resumeContent;
    private String questionType;
    private Integer count;
    private String difficulty;
}
