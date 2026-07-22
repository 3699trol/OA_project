package com.recruitment.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.job.entity.Job;
import com.recruitment.job.entity.JobCategory;
import com.recruitment.job.mapper.JobCategoryMapper;
import com.recruitment.job.mapper.JobMapper;
import com.recruitment.job.service.JobCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 职位分类服务实现
 */
@Service
@RequiredArgsConstructor
public class JobCategoryServiceImpl implements JobCategoryService {

    private final JobCategoryMapper jobCategoryMapper;
    private final JobMapper jobMapper;

    @Override
    public List<JobCategory> listAll() {
        LambdaQueryWrapper<JobCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobCategory::getDeleted, 0)
               .orderByDesc(JobCategory::getCreateTime);
        List<JobCategory> categories = jobCategoryMapper.selectList(wrapper);

        // 实时统计每个分类的关联职位数
        for (JobCategory category : categories) {
            LambdaQueryWrapper<Job> jobWrapper = new LambdaQueryWrapper<>();
            jobWrapper.eq(Job::getCategory, category.getName())
                      .eq(Job::getDeleted, 0);
            long count = jobMapper.selectCount(jobWrapper);
            category.setJobCount((int) count);
        }

        return categories;
    }

    @Override
    public JobCategory getById(Long id) {
        return jobCategoryMapper.selectById(id);
    }

    @Override
    public JobCategory create(JobCategory category) {
        category.setStatus(1);
        category.setDeleted(0);
        category.setCreateTime(LocalDateTime.now());
        jobCategoryMapper.insert(category);
        return category;
    }

    @Override
    public JobCategory update(Long id, JobCategory category) {
        JobCategory existing = jobCategoryMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        category.setId(id);
        category.setUpdateTime(LocalDateTime.now());
        jobCategoryMapper.updateById(category);
        return jobCategoryMapper.selectById(id);
    }

    @Override
    public void delete(Long id) {
        JobCategory existing = jobCategoryMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        // 使用 deleteById，MyBatis-Plus 全局逻辑删除配置会自动将其转为 UPDATE SET deleted=1
        jobCategoryMapper.deleteById(id);
    }

    @Override
    public void toggleStatus(Long id, Integer status) {
        JobCategory existing = jobCategoryMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        existing.setStatus(status);
        existing.setUpdateTime(LocalDateTime.now());
        jobCategoryMapper.updateById(existing);
    }
}
