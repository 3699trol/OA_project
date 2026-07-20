package com.recruitment.application.controller;

import com.recruitment.application.service.ApplicationService;
import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.core.model.Result;
import com.recruitment.common.security.model.LoginUser;
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
     */
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        // 候选人只能查看自己的投递记录
        PageResult<Map<String, Object>> result = applicationService.listApplications(page, size, userId, status, keyword);
        return Result.success(result);
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
