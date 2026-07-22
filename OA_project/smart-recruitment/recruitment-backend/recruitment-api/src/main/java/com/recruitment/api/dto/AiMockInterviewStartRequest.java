package com.recruitment.api.dto;

import lombok.Data;

/**
 * AI模拟面试-开始面试请求
 */
@Data
public class AiMockInterviewStartRequest {
    /** 职位ID */
    private Long jobId;
    /** 职位名称 */
    private String jobTitle;
    /** 任职要求 */
    private String jobRequirements;
    /** 岗位描述 */
    private String jobDescription;
    /** 候选人简历数据 */
    private Object resumeData;
    /** 面试类型：technical / behavioral / mixed */
    private String type;
    /** 难度：beginner / medium / advanced */
    private String difficulty;
    /** 计划题目数量 */
    private Integer count;
}
