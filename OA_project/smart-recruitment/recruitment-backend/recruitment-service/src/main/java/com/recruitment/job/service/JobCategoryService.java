package com.recruitment.job.service;

import com.recruitment.job.entity.JobCategory;

import java.util.List;

/**
 * 职位分类服务接口
 */
public interface JobCategoryService {

    /**
     * 查询所有启用的分类列表
     */
    List<JobCategory> listAll();

    /**
     * 根据ID查询分类
     */
    JobCategory getById(Long id);

    /**
     * 新增分类
     */
    JobCategory create(JobCategory category);

    /**
     * 更新分类
     */
    JobCategory update(Long id, JobCategory category);

    /**
     * 删除分类（逻辑删除）
     */
    void delete(Long id);

    /**
     * 切换分类状态
     */
    void toggleStatus(Long id, Integer status);
}
