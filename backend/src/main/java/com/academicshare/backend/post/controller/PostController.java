package com.academicshare.backend.post.controller;

import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.common.response.PageResponse;
import com.academicshare.backend.post.dto.LikeResponse;
import com.academicshare.backend.post.dto.PostCreateRequest;
import com.academicshare.backend.post.dto.PostListQuery;
import com.academicshare.backend.post.dto.PostResponse;
import com.academicshare.backend.post.dto.PostUpdateRequest;
import com.academicshare.backend.post.service.PostService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public PageResponse<PostResponse> getPostList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String author,
            @RequestParam(name = "main_category", required = false) String mainCategory,
            @RequestParam(name = "sub_category", required = false) String subCategory
    ) {
        return postService.getPostList(new PostListQuery(
                page,
                size,
                keyword,
                author,
                mainCategory,
                subCategory
        ));
    }

    @GetMapping("/{postId}")
    public PostResponse getPostDetail(@PathVariable Integer postId) {
        return postService.getPostDetail(postId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPostByMultipart(MultipartHttpServletRequest request) {
        return postService.createPost(new PostCreateRequest(
                parameter(request, "title"),
                parameter(request, "content"),
                parameter(request, "main_category"),
                parameter(request, "sub_category"),
                booleanParameter(request, "is_anonymous", true),
                uploadedFiles(request)
        ));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPostByJson(@RequestBody JsonNode request) {
        return postService.createPost(new PostCreateRequest(
                textValue(request, "title"),
                textValue(request, "content"),
                textValue(request, "main_category"),
                textValue(request, "sub_category"),
                booleanValue(request, "is_anonymous"),
                List.of()
        ));
    }

    @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PostResponse updatePostByMultipart(
            @PathVariable Integer postId,
            MultipartHttpServletRequest request
    ) {
        return postService.updatePost(postId, new PostUpdateRequest(
                parameter(request, "title"),
                hasParameter(request, "title"),
                parameter(request, "content"),
                hasParameter(request, "content"),
                parameter(request, "main_category"),
                hasParameter(request, "main_category"),
                parameter(request, "sub_category"),
                hasParameter(request, "sub_category"),
                booleanParameter(request, "is_anonymous", false),
                hasParameter(request, "is_anonymous"),
                uploadedFiles(request)
        ));
    }

    @PatchMapping(value = "/{postId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PostResponse updatePostByJson(
            @PathVariable Integer postId,
            @RequestBody JsonNode request
    ) {
        return postService.updatePost(postId, new PostUpdateRequest(
                textValue(request, "title"),
                request.has("title"),
                textValue(request, "content"),
                request.has("content"),
                textValue(request, "main_category"),
                request.has("main_category"),
                textValue(request, "sub_category"),
                request.has("sub_category"),
                booleanValue(request, "is_anonymous"),
                request.has("is_anonymous"),
                List.of()
        ));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/likes")
    @ResponseStatus(HttpStatus.CREATED)
    public LikeResponse createLike(@PathVariable Integer postId) {
        return postService.createLike(postId);
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<Void> deleteLike(@PathVariable Integer postId) {
        postService.deleteLike(postId);
        return ResponseEntity.noContent().build();
    }

    private String parameter(HttpServletRequest request, String name) {
        return request.getParameter(name);
    }

    private boolean hasParameter(HttpServletRequest request, String name) {
        return request.getParameterMap().containsKey(name);
    }

    private Boolean booleanParameter(HttpServletRequest request, String name, boolean required) {
        if (!hasParameter(request, name)) {
            if (required) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, name + " 값이 필요합니다.");
            }
            return null;
        }

        String value = parameter(request, name);
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new ApiException(ErrorCode.VALIDATION_ERROR, name + " 값은 true 또는 false여야 합니다.");
    }

    private List<MultipartFile> uploadedFiles(MultipartHttpServletRequest request) {
        List<MultipartFile> files = new ArrayList<>();
        files.addAll(request.getFiles("files"));
        files.addAll(request.getFiles("files[]"));
        return files;
    }

    private String textValue(JsonNode request, String fieldName) {
        if (!request.has(fieldName) || request.get(fieldName).isNull()) {
            return null;
        }
        if (!request.get(fieldName).isTextual()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        return request.get(fieldName).asText();
    }

    private Boolean booleanValue(JsonNode request, String fieldName) {
        if (!request.has(fieldName) || request.get(fieldName).isNull()) {
            return null;
        }
        if (!request.get(fieldName).isBoolean()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        return request.get(fieldName).asBoolean();
    }
}
