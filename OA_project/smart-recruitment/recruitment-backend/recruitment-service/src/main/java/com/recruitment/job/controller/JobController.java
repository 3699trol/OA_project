package com.recruitment.job.controller;

import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.core.model.Result;
import com.recruitment.common.security.model.LoginUser;
import com.recruitment.job.dto.JobCreateRequest;
import com.recruitment.job.dto.JobUpdateRequest;
import com.recruitment.job.entity.Job;
import com.recruitment.job.service.JobService;
import com.recruitment.job.service.RecommendationService;
import com.recruitment.job.vo.JobRecommendVO;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职位管理控制器
 */
@RestController
@RequestMapping("/api/job")
public class JobController {

    @Autowired(required = false)
    private JobService jobService;

    @Autowired(required = false)
    private RecommendationService recommendationService;

    @GetMapping("/list")
    public Result<PageResult<Job>> list(@RequestParam(name = "page", defaultValue = "1") long page,
                                         @RequestParam(name = "size", defaultValue = "10") long size,
                                         @RequestParam(name = "keyword", required = false) String keyword,
                                         @RequestParam(name = "status", required = false) Integer status,
                                         @RequestParam(name = "category", required = false) String category,
                                         @RequestParam(name = "city", required = false) String city,
                                         @RequestParam(name = "sortBy", required = false) String sortBy,
                                         @RequestParam(name = "sortOrder", required = false) String sortOrder) {
        if (jobService == null) return Result.success(PageResult.empty(page, size));
        return Result.success(jobService.listByPage(page, size, keyword, status, category, city, sortBy, sortOrder));
    }

    /**
     * 基于当前用户简历技能标签与在招职位技能标签的重叠度推荐职位。
     */
    @GetMapping("/recommend")
    public Result<List<JobRecommendVO>> recommend(@RequestParam(name = "limit", defaultValue = "1000") int limit) {
        if (recommendationService == null) {
            return Result.success(List.of());
        }
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(recommendationService.recommendJobs(userId, limit));
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
    public Result<Job> create(@Valid @RequestBody JobCreateRequest request) {
        if (jobService == null) return Result.success();
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Job job = new Job();
        BeanUtils.copyProperties(request, job);
        job.setPublisherId(userId);
        return Result.success(jobService.create(job));
    }

    @PutMapping("/{id}")
    public Result<Job> update(@PathVariable Long id,
                              @Valid @RequestBody JobUpdateRequest request) {
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

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }
}
