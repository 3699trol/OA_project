package com.recruitment.file.service.impl;

import com.recruitment.common.core.exception.BusinessException;
import com.recruitment.file.entity.FileRecord;
import com.recruitment.file.mapper.FileRecordMapper;
import com.recruitment.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 文件服务实现
 */
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRecordMapper fileRecordMapper;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.max-size:10485760}")
    private long maxSize;

    @Override
    public FileRecord upload(MultipartFile file, String fileType, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
        }

        // 将相对路径转为绝对路径，避免被 Tomcat 解析到临时目录下
        File uploadRoot = new File(uploadDir).getAbsoluteFile();

        // 直接存到 uploads/{fileType}/ 目录下
        File directory = new File(uploadRoot, fileType != null ? fileType : "other");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new BusinessException("无法创建上传目录: " + directory.getAbsolutePath());
        }

        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + extension;
        File destFile = new File(directory, storedName);

        // 保存文件
        try {
            file.transferTo(destFile.getAbsoluteFile());
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }

        // 记录到数据库
        FileRecord record = new FileRecord();
        record.setOriginalName(originalName);
        record.setStoredName(storedName);
        record.setFilePath(destFile.getAbsolutePath().replace("\\", "/"));
        record.setFileSize(file.getSize());
        record.setContentType(file.getContentType());
        record.setFileType(fileType != null ? fileType : "other");
        record.setUploaderId(uploaderId);
        record.setCreateTime(LocalDateTime.now());
        record.setDeleted(0);
        fileRecordMapper.insert(record);

        return record;
    }

    @Override
    public FileRecord getById(Long id) {
        return fileRecordMapper.selectById(id);
    }
}
