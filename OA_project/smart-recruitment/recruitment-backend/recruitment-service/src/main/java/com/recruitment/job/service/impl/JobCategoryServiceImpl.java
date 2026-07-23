package com.recruitment.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.common.redis.util.RedisUtil;
import com.recruitment.job.entity.Job;
import com.recruitment.job.entity.JobCategory;
import com.recruitment.job.mapper.JobCategoryMapper;
import com.recruitment.job.mapper.JobMapper;
import com.recruitment.job.service.JobCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 职位分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobCategoryServiceImpl implements JobCategoryService {

    private static final String CATEGORY_LIST_CACHE_KEY = "job:category:list";
    private static final long CATEGORY_LIST_CACHE_TTL_MINUTES = 10;

    private final JobCategoryMapper jobCategoryMapper;
    private final JobMapper jobMapper;
    private final ObjectProvider<RedisUtil> redisUtilProvider;

    @Override
    public List<JobCategory> listAll() {
        List<JobCategory> cached = getCachedCategoryList();
        if (cached != null) {
            return cached;
        }

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

        cacheCategoryList(categories);
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
        evictCategoryListCache();
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
        evictCategoryListCache();
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
        evictCategoryListCache();
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
        evictCategoryListCache();
    }

    @SuppressWarnings("unchecked")
    private List<JobCategory> getCachedCategoryList() {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null) {
            return null;
        }
        try {
            Object cached = redisUtil.get(CATEGORY_LIST_CACHE_KEY);
            if (cached instanceof List<?> list) {
                return (List<JobCategory>) list;
            }
        } catch (RuntimeException e) {
            log.warn("Failed to read job category list from Redis cache: {}", e.getMessage());
        }
        return null;
    }

    private void cacheCategoryList(List<JobCategory> categories) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null || categories == null) {
            return;
        }
        try {
            redisUtil.set(CATEGORY_LIST_CACHE_KEY, categories, CATEGORY_LIST_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (RuntimeException e) {
            log.warn("Failed to write job category list to Redis cache: {}", e.getMessage());
        }
    }

    private void evictCategoryListCache() {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null) {
            return;
        }
        try {
            redisUtil.delete(CATEGORY_LIST_CACHE_KEY);
        } catch (RuntimeException e) {
            log.warn("Failed to evict job category list Redis cache: {}", e.getMessage());
        }
    }
}
