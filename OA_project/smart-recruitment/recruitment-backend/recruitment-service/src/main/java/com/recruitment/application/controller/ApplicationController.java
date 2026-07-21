package com.recruitment.application.controller;

import com.recruitment.application.service.ApplicationService;
import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.core.model.Result;
import com.recruitment.common.security.model.LoginUser;
import com.recruitment.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 投递管理控制器
 */
@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ResumeService resumeService;

    /**
     * 投递职位
     */
    @PostMapping
    public Result<?> apply(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Object jobIdObj = body.get("jobId");
        if (jobIdObj == null) {
            return Result.error(400, "职位ID不能为空");
        }
        Long jobId = Long.valueOf(jobIdObj.toString());
        Map<String, Object> result = applicationService.apply(userId, jobId);
        return Result.success(result);
    }

    /**
     * 查询投递记录列表
     * @param role 角色：hr-HR查看所有候选人，candidate-候选人查看自己的（默认）
     */
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "candidate") String role) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        // HR查看所有候选人投递，不限制userId；候选人只看自己的
        Long filterUserId = "hr".equalsIgnoreCase(role) ? null : userId;
        PageResult<Map<String, Object>> result = applicationService.listApplications(page, size, filterUserId, status, keyword);
        return Result.success(result);
    }

    /**
     * 获取候选人详情（HR查看）
     */
    @GetMapping("/{id}/detail")
    public Result<Map<String, Object>> getCandidateDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Map<String, Object> detail = applicationService.getCandidateDetail(id);
        return Result.success(detail);
    }

    /**
     * 修改投递状态（HR操作）
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Object statusObj = body.get("status");
        if (statusObj == null) {
            return Result.error(400, "状态不能为空");
        }
        Integer status = Integer.valueOf(statusObj.toString());
        applicationService.updateStatus(id, status, userId);
        return Result.success();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }
}
