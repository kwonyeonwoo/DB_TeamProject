package com.academicshare.backend.notification.domain;

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

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @NotBlank
    @Column(name = "comment_content", nullable = false, columnDefinition = "TEXT")
    private String commentContent;

    @NotNull
    @Column(name = "commented_post_id", nullable = false)
    private Integer commentedPostId;

    @NotNull
    @Column(name = "commented_user_id", nullable = false)
    private Integer commentedUserId;

    @Column(name = "commented_id")
    private Integer commentedId;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Notification() {
    }

    public Notification(String commentContent, Integer commentedPostId, Integer commentedUserId, Integer commentedId) {
        this.commentContent = commentContent;
        this.commentedPostId = commentedPostId;
        this.commentedUserId = commentedUserId;
        this.commentedId = commentedId;
        this.isRead = false;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public Integer getId() {
        return id;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public Integer getCommentedPostId() {
        return commentedPostId;
    }

    public Integer getCommentedUserId() {
        return commentedUserId;
    }

    public Integer getCommentedId() {
        return commentedId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
