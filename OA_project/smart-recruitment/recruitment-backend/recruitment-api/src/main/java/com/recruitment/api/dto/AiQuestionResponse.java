package com.recruitment.api.dto;

import lombok.Data;
import java.util.List;

/**
 * AI面试题生成响应
 */
@Data
public class AiQuestionResponse {
    private List<Question> questions;

    @Data
    public static class Question {
        private String title;
        private String type;
        private String difficulty;
        private String referenceAnswer;
        private String scoringCriteria;
    }
}
