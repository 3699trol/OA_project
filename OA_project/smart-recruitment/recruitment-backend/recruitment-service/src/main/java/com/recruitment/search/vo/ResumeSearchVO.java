package com.recruitment.search.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ResumeSearchVO {
    private Long id;
    private Long userId;
    private String parsedContent;
    private String selfEvaluation;
    private String aiAnalysis;
    private BigDecimal aiScore;
    private String fileUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Map<String, List<String>> highlights;
}
