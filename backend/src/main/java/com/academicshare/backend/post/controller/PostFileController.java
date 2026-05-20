package com.academicshare.backend.post.controller;

import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.post.domain.PostFile;
import com.academicshare.backend.post.domain.PostFileId;
import com.academicshare.backend.post.repository.PostFileRepository;
import com.academicshare.backend.post.service.PostFileStorage;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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
        PostFile postFile = postFileRepository.findById(new PostFileId(postId, fileUrl))
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));

        Path filePath = postFileStorage.resolve(postId, fileName);
        if (!Files.isRegularFile(filePath)) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            return ResponseEntity.ok()
                    .contentType(resolveContentType(postFile.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(postFile, fileName))
                    .body(resource);
        } catch (MalformedURLException exception) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    private MediaType resolveContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String contentDisposition(PostFile postFile, String storedFileName) {
        String downloadFileName = StringUtils.hasText(postFile.getFileName())
                ? postFile.getFileName()
                : storedFileName;
        return ContentDisposition.attachment()
                .filename(downloadFileName, StandardCharsets.UTF_8)
                .build()
                .toString();
    }
}
