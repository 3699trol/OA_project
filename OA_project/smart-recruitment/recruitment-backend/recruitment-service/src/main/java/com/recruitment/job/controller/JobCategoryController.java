package com.recruitment.job.controller;

import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 职位分类管理控制器
 */
@RestController
@RequestMapping("/api/job/category")
@RequiredArgsConstructor
public class JobCategoryController {

    @GetMapping("/list")
    public Result<?> list() {
        // TODO: 职位分类列表
        return Result.success();
    }

    @PostMapping
    public Result<?> create() {
        // TODO: 新增职位分类
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id) {
        // TODO: 更新职位分类
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        // TODO: 删除职位分类
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        // TODO: 启用/停用职位分类
        return Result.success();
    }
}
