package com.recruitment.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.job.entity.Job;
import com.recruitment.job.mapper.JobMapper;
import com.recruitment.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 职位服务实现
 */
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobMapper jobMapper;

    @Override
    public Page<Job> listByPage(long pageNum, long pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Job::getJobName, keyword)
                    .or()
                    .like(Job::getDescription, keyword)
                    .or()
                    .like(Job::getRequirements, keyword));
        }
        if (status != null) {
            wrapper.eq(Job::getStatus, status);
        }
        wrapper.orderByDesc(Job::getCreateTime);
        return jobMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Job getById(Long id) {
        return jobMapper.selectById(id);
    }

    @Override
    public Job create(Job job) {
        job.setStatus(0); // 默认草稿
        job.setCreateTime(LocalDateTime.now());
        jobMapper.insert(job);
        return job;
    }

    @Override
    public Job update(Long id, Job job) {
        Job existing = jobMapper.selectById(id);
        if (existing == null) {
            return null;
        }
        job.setId(id);
        job.setUpdateTime(LocalDateTime.now());
        jobMapper.updateById(job);
        return jobMapper.selectById(id);
    }

    @Override
    public void publish(Long id) {
        Job job = jobMapper.selectById(id);
        if (job != null) {
            job.setStatus(1);
            job.setPublishTime(LocalDateTime.now());
            jobMapper.updateById(job);
        }
    }

    @Override
    public void unpublish(Long id) {
        Job job = jobMapper.selectById(id);
        if (job != null) {
            job.setStatus(2);
            jobMapper.updateById(job);
        }
    }
}
