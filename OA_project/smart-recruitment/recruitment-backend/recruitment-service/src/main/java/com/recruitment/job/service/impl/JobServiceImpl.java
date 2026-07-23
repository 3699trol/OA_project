package com.recruitment.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.redis.util.RedisUtil;
import com.recruitment.job.entity.Job;
import com.recruitment.job.mapper.JobMapper;
import com.recruitment.job.service.JobService;
import com.recruitment.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private static final String JOB_DETAIL_CACHE_PREFIX = "job:detail:";
    private static final String JOB_LIST_CACHE_PREFIX = "job:list:";
    private static final long JOB_DETAIL_CACHE_TTL_MINUTES = 10;
    private static final long JOB_LIST_CACHE_TTL_MINUTES = 5;

    private final JobMapper jobMapper;
    private final SearchService searchService;
    private final ObjectProvider<RedisUtil> redisUtilProvider;

    @Override
    public PageResult<Job> listByPage(long pageNum, long pageSize, String keyword, Integer status, String category) {
        String cacheKey = jobListCacheKey(pageNum, pageSize, keyword, status, category);
        PageResult<Job> cached = getCachedJobPage(cacheKey);
        if (cached != null) {
            return cached;
        }

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
        if (StringUtils.hasText(category)) {
            wrapper.eq(Job::getCategory, category);
        }
        wrapper.orderByDesc(Job::getCreateTime);
        Page<Job> jobPage = jobMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        PageResult<Job> result = new PageResult<>(jobPage.getRecords(), jobPage.getTotal(), pageNum, pageSize);
        cacheJobPage(cacheKey, result);
        return result;
    }

    @Override
    public Job getById(Long id) {
        Job cached = getCachedJob(id);
        if (cached != null) {
            return cached;
        }

        Job job = jobMapper.selectById(id);
        cacheJob(job);
        return job;
    }

    @Override
    public Job create(Job job) {
        job.setStatus(0);
        job.setCreateTime(LocalDateTime.now());
        jobMapper.insert(job);
        syncJobIndex(job);
        evictJobListCache();
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
        Job updated = jobMapper.selectById(id);
        syncJobIndex(updated);
        cacheJob(updated);
        evictJobListCache();
        return updated;
    }

    @Override
    public void publish(Long id) {
        Job job = jobMapper.selectById(id);
        if (job != null) {
            job.setStatus(1);
            job.setPublishTime(LocalDateTime.now());
            jobMapper.updateById(job);
            syncJobIndex(job);
            cacheJob(job);
            evictJobListCache();
        }
    }

    @Override
    public void unpublish(Long id) {
        Job job = jobMapper.selectById(id);
        if (job != null) {
            job.setStatus(2);
            jobMapper.updateById(job);
            syncJobIndex(job);
            cacheJob(job);
            evictJobListCache();
        }
    }

    private void syncJobIndex(Job job) {
        try {
            searchService.syncJob(job);
        } catch (RuntimeException e) {
            log.warn("Failed to sync job {} to Elasticsearch: {}", job != null ? job.getId() : null, e.getMessage());
        }
    }

    private Job getCachedJob(Long id) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null || id == null) {
            return null;
        }
        try {
            Object cached = redisUtil.get(jobDetailCacheKey(id));
            if (cached instanceof Job job) {
                return job;
            }
        } catch (RuntimeException e) {
            log.warn("Failed to read job {} from Redis cache: {}", id, e.getMessage());
        }
        return null;
    }

    private void cacheJob(Job job) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null || job == null || job.getId() == null) {
            return;
        }
        try {
            redisUtil.set(jobDetailCacheKey(job.getId()), job, JOB_DETAIL_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (RuntimeException e) {
            log.warn("Failed to write job {} to Redis cache: {}", job.getId(), e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private PageResult<Job> getCachedJobPage(String cacheKey) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null) {
            return null;
        }
        try {
            Object cached = redisUtil.get(cacheKey);
            if (cached instanceof PageResult<?> pageResult) {
                return (PageResult<Job>) pageResult;
            }
        } catch (RuntimeException e) {
            log.warn("Failed to read job page from Redis cache {}: {}", cacheKey, e.getMessage());
        }
        return null;
    }

    private void cacheJobPage(String cacheKey, PageResult<Job> result) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null || result == null) {
            return;
        }
        try {
            redisUtil.set(cacheKey, result, JOB_LIST_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (RuntimeException e) {
            log.warn("Failed to write job page to Redis cache {}: {}", cacheKey, e.getMessage());
        }
    }

    private void evictJobListCache() {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null) {
            return;
        }
        try {
            redisUtil.deleteByPattern(JOB_LIST_CACHE_PREFIX + "*");
        } catch (RuntimeException e) {
            log.warn("Failed to evict job list Redis cache: {}", e.getMessage());
        }
    }

    private String jobDetailCacheKey(Long id) {
        return JOB_DETAIL_CACHE_PREFIX + id;
    }

    private String jobListCacheKey(long pageNum, long pageSize, String keyword, Integer status, String category) {
        return JOB_LIST_CACHE_PREFIX
                + pageNum + ":"
                + pageSize + ":"
                + normalizeCachePart(keyword) + ":"
                + (status == null ? "all" : status) + ":"
                + normalizeCachePart(category);
    }

    private String normalizeCachePart(String value) {
        if (!StringUtils.hasText(value)) {
            return "all";
        }
        return value.trim().toLowerCase().replaceAll("\\s+", "_");
    }
}
