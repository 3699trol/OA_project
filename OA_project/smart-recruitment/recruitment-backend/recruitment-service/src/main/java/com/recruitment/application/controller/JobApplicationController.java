package com.recruitment.application.controller;

import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 职位投递控制器
 */
@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class JobApplicationController {

    @PostMapping
    public Result<?> create() {
        // TODO: 投递职位
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<?>> list(@RequestParam(name = "page", defaultValue = "1") long page,
                                       @RequestParam(name = "size", defaultValue = "10") long size,
                                       @RequestParam(name = "jobId", required = false) Long jobId,
                                       @RequestParam(name = "status", required = false) Integer status) {
        // TODO: 投递记录（支持职位、状态筛选）
        return Result.success(PageResult.empty(page, size));
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id) {
        // TODO: 修改投递状态
        return Result.success();
    }
}
