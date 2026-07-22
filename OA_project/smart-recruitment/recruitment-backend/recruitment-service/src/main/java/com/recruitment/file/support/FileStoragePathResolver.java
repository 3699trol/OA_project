package com.recruitment.file.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves upload paths consistently from the project root when configured with a relative path.
 */
@Component
public class FileStoragePathResolver {

    private final Path projectRoot;
    private final Path uploadRoot;

    public FileStoragePathResolver(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.projectRoot = findProjectRoot();
        Path configuredPath = Paths.get(uploadDir);
        this.uploadRoot = (configuredPath.isAbsolute() ? configuredPath : projectRoot.resolve(configuredPath))
                .toAbsolutePath()
                .normalize();
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }

    public boolean storesProjectRelativePaths() {
        return uploadRoot.startsWith(projectRoot);
    }

    public Path resolveRecordPath(String filePath) {
        Path path = Paths.get(filePath);
        return (path.isAbsolute() ? path : projectRoot.resolve(path)).toAbsolutePath().normalize();
    }

    public String toRecordPath(Path physicalFile) {
        Path normalizedFile = physicalFile.toAbsolutePath().normalize();
        if (normalizedFile.startsWith(projectRoot)) {
            return projectRoot.relativize(normalizedFile).toString().replace("\\", "/");
        }
        return normalizedFile.toString().replace("\\", "/");
    }

    private Path findProjectRoot() {
        Path current = Paths.get("").toAbsolutePath().normalize();
        for (Path path = current; path != null; path = path.getParent()) {
            if (Files.isDirectory(path.resolve("recruitment-backend"))
                    && Files.isDirectory(path.resolve("recruitment-web"))) {
                return path;
            }
        }
        return current;
    }
}
