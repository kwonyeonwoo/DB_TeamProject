package com.academicshare.backend.schedule.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "group_id")
    private Integer groupId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @NotNull
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @NotNull
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(name = "type", nullable = false)
    private Integer type;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Schedule() {
    }

    public Schedule(Integer userId, Integer groupId, String title, LocalDateTime startAt, LocalDateTime endAt, String description, Integer type) {
        this.userId = userId;
        this.groupId = groupId;
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
        this.description = description;
        this.type = type;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @AssertTrue(message = "end_at must be greater than or equal to start_at")
    boolean isPeriodValid() {
        return startAt == null || endAt == null || !endAt.isBefore(startAt);
    }

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public String getDescription() {
        return description;
    }

    public Integer getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isPersonal() {
        return groupId == null;
    }

    public boolean isOwnedBy(Integer userId) {
        return Objects.equals(this.userId, userId);
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public void changeEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeType(Integer type) {
        this.type = type;
    }

    public void markUpdated() {
        this.updatedAt = LocalDateTime.now();
    }
}
