package com.recruitment.file.support;

import com.recruitment.file.entity.FileRecord;
import com.recruitment.file.mapper.FileRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Converts legacy absolute upload paths in file_record to project-relative paths.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileRecordPathNormalizer implements ApplicationRunner {

    private final FileRecordMapper fileRecordMapper;
    private final FileStoragePathResolver pathResolver;

    @Override
    public void run(ApplicationArguments args) {
        if (!pathResolver.storesProjectRelativePaths()) {
            return;
        }

        try {
            List<FileRecord> records = fileRecordMapper.selectList(null);
            int updated = 0;
            for (FileRecord record : records) {
                String normalizedPath = normalizeUploadPath(record.getFilePath());
                if (!Objects.equals(record.getFilePath(), normalizedPath)) {
                    if (!copyLegacyFileIfNecessary(record.getFilePath(), normalizedPath)) {
                        continue;
                    }
                    record.setFilePath(normalizedPath);
                    fileRecordMapper.updateById(record);
                    updated++;
                }
            }

            if (updated > 0) {
                log.info("Normalized {} file_record file paths to project-relative uploads paths", updated);
            }
        } catch (RuntimeException e) {
            log.warn("Skipped file_record path normalization: {}", e.getMessage());
        }
    }

    private String normalizeUploadPath(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return filePath;
        }

        String normalizedPath = filePath.replace("\\", "/");
        if (normalizedPath.startsWith("uploads/")) {
            return normalizedPath;
        }

        int uploadsIndex = normalizedPath.lastIndexOf("/uploads/");
        if (uploadsIndex >= 0) {
            return normalizedPath.substring(uploadsIndex + 1);
        }

        return normalizedPath;
    }

    private boolean copyLegacyFileIfNecessary(String legacyPath, String normalizedPath) {
        if (legacyPath == null || legacyPath.isBlank()) {
            return true;
        }

        Path legacyPhysicalPath = Path.of(legacyPath);
        if (!legacyPhysicalPath.isAbsolute() || !Files.exists(legacyPhysicalPath)) {
            return true;
        }

        Path targetPath = pathResolver.resolveRecordPath(normalizedPath);
        if (Files.exists(targetPath)) {
            return true;
        }

        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(legacyPhysicalPath, targetPath);
            return true;
        } catch (IOException e) {
            log.warn("Failed to copy legacy upload file from {} to {}: {}",
                    legacyPhysicalPath, targetPath, e.getMessage());
            return false;
        }
    }
}
