package com.academicshare.backend.post.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @NotNull
    @Size(max = 255)
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @NotNull
    @PositiveOrZero
    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @NotNull
    @Size(max = 100)
    @Column(name = "main_category", nullable = false, length = 100)
    private String mainCategory;

    @NotNull
    @Size(max = 100)
    @Column(name = "sub_category", nullable = false, length = 100)
    private String subCategory;

    @NotNull
    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous;

    protected Post() {
    }

    public Post(Integer userId, String title, String content, String mainCategory, String subCategory, Boolean isAnonymous) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.mainCategory = mainCategory;
        this.subCategory = subCategory;
        this.isAnonymous = isAnonymous;
        this.viewCount = 0;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (viewCount == null) {
            viewCount = 0;
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

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public String getMainCategory() {
        return mainCategory;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public boolean isOwnedBy(Integer userId) {
        return Objects.equals(this.userId, userId);
    }

    public void increaseViewCount() {
        viewCount = viewCount == null ? 1 : viewCount + 1;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void changeMainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
    }

    public void changeSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public void changeIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public void markUpdated() {
        updatedAt = LocalDateTime.now();
    }
}
