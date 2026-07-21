package com.recruitment.application.service;

import com.recruitment.common.core.model.PageResult;

import java.util.Map;

/**
 * 投递服务接口
 */
public interface ApplicationService {

    /**
     * 投递职位
     * @param userId 求职者ID
     * @param jobId 职位ID
     * @return 投递记录
     */
    Map<String, Object> apply(Long userId, Long jobId);

    /**
     * 查询投递记录列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 求职者ID（候选人查自己的）
     * @param status 状态筛选
     * @param keyword 关键字搜索
     * @return 分页结果
     */
    PageResult<Map<String, Object>> listApplications(long pageNum, long pageSize, Long userId, Integer status, String keyword);

    /**
     * 获取候选人详情（HR查看）
     * @param applicationId 投递记录ID
     * @return 候选人信息 + 简历数据
     */
    Map<String, Object> getCandidateDetail(Long applicationId);

    /**
     * 修改投递状态
     * @param id 投递记录ID
     * @param status 新状态
     * @param operatorId 操作人ID
     */
    void updateStatus(Long id, Integer status, Long operatorId);
}
