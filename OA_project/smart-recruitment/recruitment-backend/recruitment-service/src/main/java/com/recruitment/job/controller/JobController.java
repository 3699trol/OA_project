package com.recruitment.job.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.core.model.Result;
import com.recruitment.job.dto.JobCreateRequest;
import com.recruitment.job.dto.JobUpdateRequest;
import com.recruitment.job.entity.Job;
import com.recruitment.job.service.JobService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 职位管理控制器
 */
@RestController
@RequestMapping("/api/job")
public class JobController {

    @Autowired(required = false)
    private JobService jobService;

    @GetMapping("/list")
    public Result<PageResult<Job>> list(@RequestParam(name = "page", defaultValue = "1") long page,
                                         @RequestParam(name = "size", defaultValue = "10") long size,
                                         @RequestParam(name = "keyword", required = false) String keyword,
                                         @RequestParam(name = "status", required = false) Integer status,
                                         @RequestParam(name = "category", required = false) String category) {
        if (jobService == null) return Result.success(PageResult.empty(page, size));
        Page<Job> jobPage = jobService.listByPage(page, size, keyword, status, category);
        return Result.success(new PageResult<>(jobPage.getRecords(), jobPage.getTotal(), page, size));
    }

    @GetMapping("/{id}")
    public Result<Job> getById(@PathVariable Long id) {
        if (jobService == null) return Result.success();
        Job job = jobService.getById(id);
        if (job == null) {
            return Result.error(404, "职位不存在");
        }
        return Result.success(job);
    }

    @PostMapping
    public Result<Job> create(@RequestBody JobCreateRequest request) {
        if (jobService == null) return Result.success();
        Job job = new Job();
        BeanUtils.copyProperties(request, job);
        return Result.success(jobService.create(job));
    }

    @PutMapping("/{id}")
    public Result<Job> update(@PathVariable Long id, @RequestBody JobUpdateRequest request) {
        if (jobService == null) return Result.success();
        Job job = new Job();
        BeanUtils.copyProperties(request, job);
        job = jobService.update(id, job);
        if (job == null) {
            return Result.error(404, "职位不存在");
        }
        return Result.success(job);
    }

    @PostMapping("/{id}/publish")
    public Result<?> publish(@PathVariable Long id) {
        if (jobService == null) return Result.success();
        Job job = jobService.getById(id);
        if (job == null) {
            return Result.error(404, "职位不存在");
        }
        jobService.publish(id);
        return Result.success();
    }

    @PostMapping("/{id}/unpublish")
    public Result<?> unpublish(@PathVariable Long id) {
        if (jobService == null) return Result.success();
        Job job = jobService.getById(id);
        if (job == null) {
            return Result.error(404, "职位不存在");
        }
        jobService.unpublish(id);
        return Result.success();
    }
}
