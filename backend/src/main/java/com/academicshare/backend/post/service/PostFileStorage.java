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
        this.uploadRoot = Path.of(uploadRoot);
    }

    public String store(Integer postId, MultipartFile file) {
        String storedName = UUID.randomUUID().toString();
        Path directory = uploadRoot.resolve("posts").resolve(postId.toString());
        Path target = directory.resolve(storedName);

        try {
            Files.createDirectories(directory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Failed to upload file.");
        }

        return "/uploads/posts/" + postId + "/" + storedName;
    }
}
