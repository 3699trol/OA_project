package com.recruitment.api.dto;

import lombok.Data;
import java.util.List;

/**
 * AI模拟面试-对话响应
 */
@Data
public class AiMockInterviewChatResponse {
    /** AI面试官回复 */
    private String reply;
    /** 建议候选人可以反问的问题 */
    private List<String> suggestedQuestions;
}
