package com.recruitment.api.dto;

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
        /** 详细评价与改进建议 */
        private String details;
    }

    @Data
    public static class DimensionScores {
        /** 基础能力 */
        private Integer base;
        /** 框架工具熟练度 */
        private Integer framework;
        /** 系统设计能力 */
        private Integer design;
        /** 沟通表达能力 */
        private Integer communication;
    }
}
