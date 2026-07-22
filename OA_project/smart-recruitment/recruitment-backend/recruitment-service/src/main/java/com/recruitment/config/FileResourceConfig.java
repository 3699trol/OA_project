package com.recruitment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 静态资源映射配置：将上传的文件目录映射到 /uploads/** 路径，
 * 便于通过 URL 直接访问文件（如图片预览）。文件下载仍优先使用 /api/file/download/{id}。
 */
@Configuration
public class FileResourceConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = new File(uploadDir).getAbsolutePath().replace("\\", "/");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + location + "/");
    }
}
