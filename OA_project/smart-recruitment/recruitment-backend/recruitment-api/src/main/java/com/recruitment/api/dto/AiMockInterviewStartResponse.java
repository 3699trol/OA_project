package com.recruitment.api.dto;

import lombok.Data;

/**
 * AI模拟面试-开始面试响应
 */
@Data
public class AiMockInterviewStartResponse {
    /** 面试会话ID */
    private String sessionId;
    /** 欢迎语/开场白 */
    private String welcomeMessage;
}
