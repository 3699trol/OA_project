package com.recruitment.file.controller;

import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理控制器
 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file) {
        // TODO: 文件上传
        return Result.success();
    }

    @GetMapping("/download/{id}")
    public Result<?> download(@PathVariable Long id) {
        // TODO: 文件下载
        return Result.success();
    }
}
