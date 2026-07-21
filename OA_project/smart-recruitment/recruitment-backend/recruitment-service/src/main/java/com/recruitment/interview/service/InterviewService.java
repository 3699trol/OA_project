package com.recruitment.interview.service;

import com.recruitment.common.core.model.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 面试服务
 */
public interface InterviewService {

    /**
     * 创建面试
     */
    Map<String, Object> createInterview(Map<String, Object> params, Long operatorId);

    /**
     * 面试列表（HR查看所有）
     */
    PageResult<Map<String, Object>> listInterviews(long pageNum, long pageSize, Integer status);

    /**
     * 获取创建面试的下拉选项（候选人列表 + 面试官列表）
     */
    Map<String, Object> getOptions();

    /**
     * 获取候选人的投递记录（用于选择面试职位）
     */
    List<Map<String, Object>> getCandidateApplications(Long userId);

    /**
     * 面试官查看自己的面试任务列表
     */
    PageResult<Map<String, Object>> listByInterviewer(long pageNum, long pageSize, Integer status, Long interviewerId);

    /**
     * 面试详情（HR/面试官通用）
     */
    Map<String, Object> getInterviewDetail(Long interviewId);

    /**
     * 保存面试评价（面试官提交）
     */
    void saveEvaluation(Map<String, Object> params, Long interviewerId);

    /**
     * 根据面试ID查询评价
     */
    Map<String, Object> getEvaluationByInterviewId(Long interviewId);

    /**
     * 取消面试
     */
    void cancelInterview(Long interviewId, Long operatorId);

    /**
     * HR处理面试结果（录用/淘汰）
     */
    void processResult(Long interviewId, Integer hireDecision, Long operatorId);
}
