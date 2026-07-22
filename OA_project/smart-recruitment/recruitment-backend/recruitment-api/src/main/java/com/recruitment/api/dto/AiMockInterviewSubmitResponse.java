package com.recruitment.api.dto;

import java.util.List;

import lombok.Data;

/**
 * AI模拟面试-提交/结束面试响应（评估报告）
 */
@Data
public class AiMockInterviewSubmitResponse {
    /** 报告ID */
    private Long id;
    /** 职位名称 */
    private String jobTitle;
    /** 面试时长描述 */
    private String duration;
    /** 综合评分 0-100 */
    private Integer score;
    /** 报告生成时间 */
    private String time;
    /** 详细报告 */
    private ReportData report;

    @Data
    public static class ReportData {
        /** 整体评价 */
        private String overall;
        /** 各维度评分 */
        private DimensionScores scores;
        /** 详细评价（对话记录等） */
        private String details;
        /** 薄弱环节与进阶建议（AI动态生成） */
        private List<String> weaknesses;
    }

    @Data
    public static class DimensionScores {
        /** 技术能力（基础知识+框架+架构） */
        private Integer tech;
        /** 逻辑思维 */
        private Integer logic;
        /** 表达沟通 */
        private Integer communication;
    }
}
