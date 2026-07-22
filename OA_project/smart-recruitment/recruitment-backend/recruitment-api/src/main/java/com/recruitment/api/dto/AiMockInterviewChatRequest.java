package com.recruitment.api.dto;

import lombok.Data;

/**
 * AI模拟面试-对话请求
 */
@Data
public class AiMockInterviewChatRequest {
    /** 面试会话ID */
    private String sessionId;
    /** 用户最新消息 */
    private String message;
    /** 当前对话轮次（从0开始） */
    private Integer chatCount;
    /** 职位名称 */
    private String jobTitle;
}
