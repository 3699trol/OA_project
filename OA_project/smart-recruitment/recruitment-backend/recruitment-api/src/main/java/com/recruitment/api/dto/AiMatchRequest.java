package com.recruitment.api.dto;

import lombok.Data;

/**
 * AI人岗匹配请求
 */
@Data
public class AiMatchRequest {
    private Long resumeId;
    private Long jobId;
    private String resumeContent;
    private String jobContent;
}
