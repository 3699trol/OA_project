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
}
