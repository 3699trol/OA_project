package com.recruitment.file.service;

import com.recruitment.file.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件
     * @param file 上传的文件
     * @param fileType 文件分类: resume/avatar/other
     * @param uploaderId 上传人ID
     * @return 文件记录
     */
    FileRecord upload(MultipartFile file, String fileType, Long uploaderId);

    /**
     * 根据ID获取文件记录
     */
    FileRecord getById(Long id);

    /**
     * 根据ID删除文件（同时删除磁盘文件和数据库记录）
     * @param id 文件记录ID
     * @return true 表示删除成功
     */
    boolean deleteById(Long id);
}
