package com.recruitment.dashboard.controller;

import com.recruitment.common.core.model.Result;
import com.recruitment.common.security.model.LoginUser;
import com.recruitment.dashboard.service.DashboardService;
import com.recruitment.dashboard.vo.DashboardStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 仪表板控制器
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired(required = false)
    private DashboardService dashboardService;

    /**
     * HR 工作台统计数据
     */
    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats() {
        if (dashboardService == null) {
            return Result.success(new DashboardStatsVO());
        }
        return Result.success(dashboardService.getStats());
    }

    /**
     * 面试官工作台统计数据
     */
    @GetMapping("/interviewer-stats")
    public Result<DashboardStatsVO> getInterviewerStats() {
        if (dashboardService == null) {
            return Result.success(new DashboardStatsVO());
        }
        Long interviewerId = getCurrentUserId();
        if (interviewerId == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(dashboardService.getInterviewerStats(interviewerId));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }
}
