package com.recruitment.api.dto;

import lombok.Data;
import java.util.List;

/**
 * AI人岗匹配响应
 */
@Data
public class AiMatchResponse {
    private Double totalScore;
    private Double skillMatchScore;
    private Double educationMatchScore;
    private Double experienceMatchScore;
    private String explanation;
    private List<String> strengths;
    private List<String> weaknesses;
}
