package com.recruitment.interview.controller;

import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 面试管理控制器
 */
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    @PostMapping
    public Result<?> create() {
        // TODO: 创建面试任务
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<?>> list(@RequestParam(name = "page", defaultValue = "1") long page,
                                       @RequestParam(name = "size", defaultValue = "10") long size,
                                       @RequestParam(name = "status", required = false) Integer status) {
        // TODO: 面试列表（支持状态筛选）
        return Result.success(PageResult.empty(page, size));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        // TODO: 面试详情
        return Result.success();
    }
}
