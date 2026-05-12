package com.academicshare.backend.post.domain;

import java.io.Serializable;
import java.util.Objects;

public class PostFileId implements Serializable {

    private Integer id;
    private String fileUrl;

    protected PostFileId() {
    }

    public PostFileId(Integer id, String fileUrl) {
        this.id = id;
        this.fileUrl = fileUrl;
    }

    public Integer getId() {
        return id;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PostFileId that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(fileUrl, that.fileUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fileUrl);
    }
}
