package com.academicshare.backend.post.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@IdClass(PostFileId.class)
@Table(name = "file")
public class PostFile {

    @Id
    @NotNull
    @Column(name = "id", nullable = false)
    private Integer id;

    @Id
    @NotNull
    @Size(max = 1024)
    @Column(name = "file_url", nullable = false, length = 1024)
    private String fileUrl;

    protected PostFile() {
    }

    public PostFile(Integer id, String fileUrl) {
        this.id = id;
        this.fileUrl = fileUrl;
    }

    public Integer getId() {
        return id;
    }

    public String getFileUrl() {
        return fileUrl;
    }
}
