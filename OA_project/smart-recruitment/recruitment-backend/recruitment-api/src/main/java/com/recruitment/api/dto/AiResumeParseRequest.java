package com.recruitment.api.dto;

import lombok.Data;

/**
 * AI简历解析请求
 */
@Data
public class AiResumeParseRequest {
    private String resumeContent;
    private String fileType;
}
