package com.recruitment.job.service;

import com.recruitment.common.core.model.PageResult;
import com.recruitment.job.entity.Job;

/**
 * 职位服务接口
 */
public interface JobService {

    /**
     * 分页查询职位列表（支持关键字、状态、类别、城市筛选与排序）
     */
    PageResult<Job> listByPage(long pageNum, long pageSize, String keyword, Integer status,
                               String category, String city, String sortBy, String sortOrder);

    /**
     * 根据ID查询职位详情
     */
    Job getById(Long id);

    /**
     * 创建职位
     */
    Job create(Job job);

    /**
     * 更新职位
     */
    Job update(Long id, Job job);

    /**
     * 发布职位
     */
    void publish(Long id);

    /**
     * 下架职位
     */
    void unpublish(Long id);
}
