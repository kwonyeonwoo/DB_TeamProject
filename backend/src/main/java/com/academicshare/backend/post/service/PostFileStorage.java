package com.academicshare.backend.post.service;

import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class PostFileStorage {

    private final Path uploadRoot;

    public PostFileStorage(@Value("${app.upload.root:uploads}") String uploadRoot) {
        this.uploadRoot = Path.of(uploadRoot).toAbsolutePath().normalize();
    }

    public String store(Integer postId, MultipartFile file) {
        String storedName = UUID.randomUUID().toString();
        Path directory = postDirectory(postId);
        Path target = directory.resolve(storedName);

        try {
            Files.createDirectories(directory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Failed to upload file.");
        }

        return fileUrl(postId, storedName);
    }

    public Path resolve(Integer postId, String storedName) {
        Path directory = postDirectory(postId);
        Path target = directory.resolve(storedName).normalize();

        if (!target.startsWith(directory)) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        return target;
    }

    public String fileUrl(Integer postId, String storedName) {
        return "/uploads/posts/" + postId + "/" + storedName;
    }

    public String resourceLocation() {
        String location = uploadRoot.toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }

    private Path postDirectory(Integer postId) {
        return uploadRoot.resolve("posts").resolve(postId.toString()).normalize();
    }
}
