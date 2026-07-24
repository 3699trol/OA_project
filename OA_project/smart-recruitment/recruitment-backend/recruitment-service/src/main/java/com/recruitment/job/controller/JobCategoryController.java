package com.recruitment.job.controller;

import com.recruitment.common.core.model.Result;
import com.recruitment.job.dto.JobCategoryRequest;
import com.recruitment.job.entity.JobCategory;
import com.recruitment.job.service.JobCategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 职位分类管理控制器
 */
@RestController
@RequestMapping("/api/job/category")
@Validated
public class JobCategoryController {

    @Autowired(required = false)
    private JobCategoryService jobCategoryService;

    /**
     * 查询职位分类列表
     */
    @GetMapping("/list")
    public Result<List<JobCategory>> list() {
        if (jobCategoryService == null) return Result.success(List.of());
        List<JobCategory> list = jobCategoryService.listAll();
        return Result.success(list);
    }

    /**
     * 查询启用的分类名称列表（用于下拉选择）
     */
    @GetMapping("/active-names")
    public Result<List<String>> activeNames() {
        if (jobCategoryService == null) return Result.success(List.of());
        List<String> names = jobCategoryService.listAll().stream()
                .filter(c -> c.getStatus() != null && c.getStatus() == 1)
                .map(JobCategory::getName)
                .collect(Collectors.toList());
        return Result.success(names);
    }

    /**
     * 新增职位分类
     */
    @PostMapping
    public Result<JobCategory> create(@Valid @RequestBody JobCategoryRequest request) {
        if (jobCategoryService == null) return Result.success();
        JobCategory category = toEntity(request);
        try {
            JobCategory created = jobCategoryService.create(category);
            return Result.success(created);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 更新职位分类
     */
    @PutMapping("/{id}")
    public Result<JobCategory> update(@PathVariable Long id,
                                      @Valid @RequestBody JobCategoryRequest request) {
        if (jobCategoryService == null) return Result.success();
        JobCategory category = toEntity(request);
        try {
            JobCategory updated = jobCategoryService.update(id, category);
            return Result.success(updated);
        } catch (RuntimeException e) {
            return Result.error(404, e.getMessage());
        }
    }

    /**
     * 删除职位分类
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        if (jobCategoryService == null) return Result.success();
        try {
            jobCategoryService.delete(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(404, e.getMessage());
        }
    }

    /**
     * 切换分类状态
     */
    @PutMapping("/{id}/status")
    public Result<?> toggleStatus(@PathVariable Long id,
                                  @RequestParam @Min(0) @Max(1) Integer status) {
        if (jobCategoryService == null) return Result.success();
        try {
            jobCategoryService.toggleStatus(id, status);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(404, e.getMessage());
        }
    }

    private JobCategory toEntity(JobCategoryRequest request) {
        JobCategory category = new JobCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setStatus(request.getStatus());
        return category;
    }
}
