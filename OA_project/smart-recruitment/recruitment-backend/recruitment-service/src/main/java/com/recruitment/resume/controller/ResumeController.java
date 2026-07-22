package com.recruitment.resume.controller;

import com.recruitment.common.core.model.Result;
import com.recruitment.common.security.model.LoginUser;
import com.recruitment.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 简历管理控制器
 */
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * 获取简历详情
     */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        // TODO: 根据ID获取简历详情（需要权限校验）
        return Result.success();
    }

    /**
     * 保存简历（新建）
     */
    @PostMapping
    public Result<?> save(@RequestBody Map<String, Object> data) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        resumeService.saveMyResume(userId, data);
        return Result.success();
    }

    /**
     * 更新简历
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        resumeService.saveMyResume(userId, data);
        return Result.success();
    }

    /**
     * 获取当前用户的简历
     */
    @GetMapping("/my")
    public Result<Map<String, Object>> getMyResume() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Map<String, Object> resume = resumeService.getMyResume(userId);
        return Result.success(resume);
    }

    /**
     * 保存/更新当前用户的简历
     */
    @PutMapping("/my")
    public Result<?> saveMyResume(@RequestBody Map<String, Object> data) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        resumeService.saveMyResume(userId, data);
        return Result.success();
    }

    /**
     * 删除当前用户的简历（含子表数据和关联文件）
     */
    @DeleteMapping("/my")
    public Result<?> deleteMyResume() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        resumeService.deleteMyResume(userId);
        return Result.success();
    }

    /**
     * AI解析简历
     */
    @PostMapping("/{id}/parse")
    public Result<?> parse(@PathVariable Long id) {
        // TODO: AI解析简历
        return Result.success();
    }

    /**
     * AI简历评估
     */
    @PostMapping("/{id}/evaluate")
    public Result<?> evaluate(@PathVariable Long id) {
        // TODO: AI简历评估
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
