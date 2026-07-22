package com.recruitment.file.controller;

import com.recruitment.common.core.model.Result;
import com.recruitment.common.security.model.LoginUser;
import com.recruitment.file.entity.FileRecord;
import com.recruitment.file.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件管理控制器
 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 上传文件
     * @param file 上传的文件
     * @param fileType 文件分类: resume/avatar/other，默认 other
     */
    @PostMapping("/upload")
    public Result<FileRecord> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileType", defaultValue = "other") String fileType) {
        Long uploaderId = getCurrentUserId();
        FileRecord record = fileService.upload(file, fileType, uploaderId);
        return Result.success(record);
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        FileRecord record = fileService.getById(id);
        if (record == null || record.getDeleted() != null && record.getDeleted() == 1) {
            throw new RuntimeException("文件不存在");
        }

        File file = new File(record.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("文件已丢失");
        }

        response.setContentType(record.getContentType() != null ? record.getContentType() : "application/octet-stream");
        response.setContentLengthLong(file.length());

        // 处理文件名编码，支持中文
        String fileName = record.getOriginalName();
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName);

        try (InputStream in = new BufferedInputStream(new FileInputStream(file));
             OutputStream out = new BufferedOutputStream(response.getOutputStream())) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件信息（不下载，只返回元数据）
     */
    @GetMapping("/info/{id}")
    public Result<FileRecord> info(@PathVariable Long id) {
        FileRecord record = fileService.getById(id);
        if (record == null) {
            return Result.error(404, "文件不存在");
        }
        return Result.success(record);
    }

    /**
     * 删除文件（同时删除磁盘文件和数据库记录）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean ok = fileService.deleteById(id);
        if (!ok) {
            return Result.error(404, "文件不存在");
        }
        return Result.success();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }
}
