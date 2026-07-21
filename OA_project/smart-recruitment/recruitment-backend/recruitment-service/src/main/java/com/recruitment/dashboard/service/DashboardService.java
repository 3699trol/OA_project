package com.recruitment.dashboard.service;

import com.recruitment.dashboard.vo.DashboardStatsVO;

/**
 * 仪表板服务接口
 */
public interface DashboardService {

    /**
     * 获取 HR 仪表板统计数据
     */
    DashboardStatsVO getStats();

    /**
     * 获取面试官仪表板统计数据
     * @param interviewerId 面试官用户ID
     */
    DashboardStatsVO getInterviewerStats(Long interviewerId);
}
