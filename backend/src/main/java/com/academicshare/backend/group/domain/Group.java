package com.academicshare.backend.group.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "`groups`")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "group_code", nullable = false, length = 255, unique = true)
    private String groupCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull
    @Column(name = "leader_id", nullable = false)
    private Integer leaderId;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Group() {
    }

    public Group(String groupCode, String name, Integer leaderId) {
        this.groupCode = groupCode;
        this.name = name;
        this.leaderId = leaderId;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Integer getId() {
        return id;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public String getName() {
        return name;
    }

    public Integer getLeaderId() {
        return leaderId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void changeLeaderId(Integer leaderId) {
        this.leaderId = leaderId;
    }
}
