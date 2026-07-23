package com.recruitment.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 仪表板统计数据 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsVO {

    /** 智能在招岗位数 */
    private Long activeJobs;

    /** 系统累计投递数 */
    private Long totalApplications;

    /** 今日投递数（Redis INCR 实时计数） */
    private Long todayApplications;

    /** 今日面试数（Redis INCR 实时计数） */
    private Long todayInterviews;

    /** 日常面试中 */
    private Long interviewing;

    /** 本月入职新员工 */
    private Long onboardingThisMonth;

    /** 投递阶段漏斗数据 */
    private List<ProgressItem> progressData;

    /** 最近6个月投递趋势 */
    private List<MonthlyTrendItem> monthlyTrend;

    /** 最新投递记录（最多10条） */
    private List<RecentApplication> recentApplications;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressItem {
        /** 状态中文名：待筛选 / 面试中 / 已录用 / 已淘汰 */
        private String status;
        /** 数量 */
        private Long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrendItem {
        /** 月份标签，例如 7月 */
        private String month;
        /** 当月投递数量 */
        private Long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentApplication {
        /** 候选人姓名 */
        private String candidateName;
        /** 投递意向岗位 */
        private String jobTitle;
        /** AI 简历契合度 */
        private Integer resumeScore;
        /** 当前所处阶段：PENDING / INTERVIEW / OFFER / REJECTED */
        private String status;
        /** 投递时间 */
        private String applyTime;
    }
}
