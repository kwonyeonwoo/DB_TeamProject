package com.academicshare.backend.comment.controller;

import com.academicshare.backend.comment.dto.CommentCreateRequest;
import com.academicshare.backend.comment.dto.CommentResponse;
import com.academicshare.backend.comment.dto.CommentUpdateRequest;
import com.academicshare.backend.comment.service.CommentService;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.common.response.ItemsResponse;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/posts/{postId}/comments")
    public ItemsResponse<CommentResponse> getComments(@PathVariable Integer postId) {
        return new ItemsResponse<>(commentService.getComments(postId));
    }

    @PostMapping(value = "/posts/{postId}/comments", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(
            @PathVariable Integer postId,
            @RequestBody JsonNode request
    ) {
        return commentService.createComment(postId, new CommentCreateRequest(
                textValue(request, "content"),
                booleanValue(request, "is_anonymous")
        ));
    }

    @PostMapping(value = "/comments/{commentId}/replies", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createReply(
            @PathVariable Integer commentId,
            @RequestBody JsonNode request
    ) {
        return commentService.createReply(commentId, new CommentCreateRequest(
                textValue(request, "content"),
                booleanValue(request, "is_anonymous")
        ));
    }

    @PatchMapping(value = "/comments/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CommentResponse updateComment(
            @PathVariable Integer commentId,
            @RequestBody JsonNode request
    ) {
        return commentService.updateComment(commentId, new CommentUpdateRequest(
                textValue(request, "content"),
                request.has("content"),
                booleanValue(request, "is_anonymous"),
                request.has("is_anonymous")
        ));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Integer commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    private String textValue(JsonNode request, String fieldName) {
        JsonNode value = request.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isTextual()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        return value.asText();
    }

    private Boolean booleanValue(JsonNode request, String fieldName) {
        JsonNode value = request.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isBoolean()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        return value.asBoolean();
    }
}
