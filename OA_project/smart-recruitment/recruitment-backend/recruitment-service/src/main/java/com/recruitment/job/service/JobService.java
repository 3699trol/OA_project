package com.recruitment.job.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.job.entity.Job;

/**
 * 职位服务接口
 */
public interface JobService {

    /**
     * 分页查询职位列表（支持关键字、状态、类别筛选）
     */
    Page<Job> listByPage(long pageNum, long pageSize, String keyword, Integer status, String category);

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
