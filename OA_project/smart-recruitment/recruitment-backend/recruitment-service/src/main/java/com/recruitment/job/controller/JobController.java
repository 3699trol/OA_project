package com.recruitment.job.controller;

import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 职位管理控制器
 */
@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
public class JobController {

    @GetMapping("/list")
    public Result<PageResult<?>> list(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Integer status) {
        // TODO: 职位列表（支持关键字、状态筛选）
        return Result.success(PageResult.empty(page, size));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        // TODO: 职位详情
        return Result.success();
    }

    @PostMapping
    public Result<?> create() {
        // TODO: 创建职位
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id) {
        // TODO: 更新职位
        return Result.success();
    }

    @PostMapping("/{id}/publish")
    public Result<?> publish(@PathVariable Long id) {
        // TODO: 发布职位
        return Result.success();
    }

    @PostMapping("/{id}/unpublish")
    public Result<?> unpublish(@PathVariable Long id) {
        // TODO: 下架职位
        return Result.success();
    }
}
