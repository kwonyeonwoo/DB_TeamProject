package com.academicshare.backend.comment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @NotNull
    @Column(name = "post_id", nullable = false)
    private Integer postId;

    @Column(name = "parent_comment")
    private Integer parentComment;

    @NotBlank
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @NotNull
    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Comment() {
    }

    public Comment(Integer userId, Integer postId, Integer parentComment, String content, Boolean isAnonymous) {
        this.userId = userId;
        this.postId = postId;
        this.parentComment = parentComment;
        this.content = content;
        this.isAnonymous = isAnonymous;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isAnonymous == null) {
            isAnonymous = false;
        }
    }

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getPostId() {
        return postId;
    }

    public Integer getParentComment() {
        return parentComment;
    }

    public String getContent() {
        return content;
    }

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isReply() {
        return parentComment != null;
    }

    public boolean isOwnedBy(Integer userId) {
        return Objects.equals(this.userId, userId);
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void changeIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public void markUpdated() {
        this.updatedAt = LocalDateTime.now();
    }
}
