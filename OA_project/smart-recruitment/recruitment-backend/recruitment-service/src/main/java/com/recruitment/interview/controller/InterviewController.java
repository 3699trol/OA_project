package com.recruitment.interview.controller;

import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.core.model.Result;
import com.recruitment.common.security.model.LoginUser;
import com.recruitment.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 面试管理控制器
 */
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /**
     * 创建面试
     */
    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Map<String, Object> result = interviewService.createInterview(body, userId);
        return Result.success(result);
    }

    /**
     * 面试列表（HR查看所有）
     */
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        PageResult<Map<String, Object>> result = interviewService.listInterviews(page, size, status);
        return Result.success(result);
    }

    /**
     * 面试官查看自己的面试任务列表
     */
    @GetMapping("/my-tasks")
    public Result<PageResult<Map<String, Object>>> myTasks(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        PageResult<Map<String, Object>> result = interviewService.listByInterviewer(page, size, status, userId);
        return Result.success(result);
    }

    /**
     * 获取创建面试的下拉选项（候选人 + 面试官）
     */
    @GetMapping("/options")
    public Result<Map<String, Object>> getOptions() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Map<String, Object> options = interviewService.getOptions();
        return Result.success(options);
    }

    /**
     * 获取候选人的投递记录（选择候选人后加载其投递职位）
     */
    @GetMapping("/candidate-applications")
    public Result<List<Map<String, Object>>> getCandidateApplications(
            @RequestParam Long userId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "未登录");
        }
        List<Map<String, Object>> applications = interviewService.getCandidateApplications(userId);
        return Result.success(applications);
    }

    /**
     * 面试详情（HR/面试官通用）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Map<String, Object> detail = interviewService.getInterviewDetail(id);
        return Result.success(detail);
    }

    /**
     * 保存面试评价（面试官提交）
     */
    @PostMapping("/evaluation")
    public Result<?> saveEvaluation(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        interviewService.saveEvaluation(body, userId);
        return Result.success("评价提交成功");
    }

    /**
     * 根据面试ID查询评价
     */
    @GetMapping("/evaluation/{interviewId}")
    public Result<Map<String, Object>> getEvaluation(@PathVariable Long interviewId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Map<String, Object> evaluation = interviewService.getEvaluationByInterviewId(interviewId);
        return Result.success(evaluation);
    }

    /**
     * 取消面试
     */
    @PostMapping("/{id}/cancel")
    public Result<?> cancelInterview(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        interviewService.cancelInterview(id, userId);
        return Result.success("面试已取消");
    }

    /**
     * HR处理面试结果（录用/淘汰）
     */
    @PostMapping("/{id}/process")
    public Result<?> processResult(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Integer hireDecision = body.get("hireDecision") != null ? Integer.valueOf(body.get("hireDecision").toString()) : null;
        interviewService.processResult(id, hireDecision, userId);
        return Result.success("操作成功");
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }
}
