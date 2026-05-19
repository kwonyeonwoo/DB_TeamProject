package com.academicshare.backend.post.controller;

import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.post.domain.PostFileId;
import com.academicshare.backend.post.repository.PostFileRepository;
import com.academicshare.backend.post.service.PostFileStorage;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts/{postId}/files")
public class PostFileController {

    private final PostFileRepository postFileRepository;
    private final PostFileStorage postFileStorage;

    public PostFileController(
            PostFileRepository postFileRepository,
            PostFileStorage postFileStorage
    ) {
        this.postFileRepository = postFileRepository;
        this.postFileStorage = postFileStorage;
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Integer postId,
            @PathVariable String fileName
    ) {
        String fileUrl = postFileStorage.fileUrl(postId, fileName);

        if (!postFileRepository.existsById(new PostFileId(postId, fileUrl))) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        Path filePath = postFileStorage.resolve(postId, fileName);
        if (!Files.isRegularFile(filePath)) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (MalformedURLException exception) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }
}
