package com.recruitment.resume.service;

import java.util.Map;

/**
 * 简历服务接口
 */
public interface ResumeService {

    /**
     * 获取当前用户的简历
     * @param userId 用户ID
     * @return 简历数据
     */
    Map<String, Object> getMyResume(Long userId);

    /**
     * 保存/更新当前用户的简历
     * @param userId 用户ID
     * @param data 简历数据
     */
    void saveMyResume(Long userId, Map<String, Object> data);

    /**
     * 删除当前用户的简历（含子表数据和关联文件）
     * @param userId 用户ID
     */
    void deleteMyResume(Long userId);
}
