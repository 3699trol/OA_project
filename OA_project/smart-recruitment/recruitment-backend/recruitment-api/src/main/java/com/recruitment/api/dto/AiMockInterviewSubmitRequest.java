package com.recruitment.api.dto;

import lombok.Data;

/**
 * AI模拟面试-提交/结束面试请求
 */
@Data
public class AiMockInterviewSubmitRequest {
    /** 面试会话ID */
    private String sessionId;
    /** 职位名称 */
    private String jobTitle;
}
