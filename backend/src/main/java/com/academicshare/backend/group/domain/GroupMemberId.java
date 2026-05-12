package com.academicshare.backend.group.domain;

import java.io.Serializable;
import java.util.Objects;

public class GroupMemberId implements Serializable {

    private Integer groupId;
    private Integer userId;

    protected GroupMemberId() {
    }

    public GroupMemberId(Integer groupId, Integer userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public Integer getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof GroupMemberId that)) {
            return false;
        }
        return Objects.equals(groupId, that.groupId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, userId);
    }
}
