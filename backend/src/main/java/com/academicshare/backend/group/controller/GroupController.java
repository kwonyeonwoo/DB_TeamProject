package com.academicshare.backend.group.controller;

import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.common.response.ItemsResponse;
import com.academicshare.backend.group.dto.GroupCreateRequest;
import com.academicshare.backend.group.dto.GroupCreateResponse;
import com.academicshare.backend.group.dto.GroupDetailResponse;
import com.academicshare.backend.group.dto.GroupJoinRequest;
import com.academicshare.backend.group.dto.GroupMemberResponse;
import com.academicshare.backend.group.dto.GroupResponse;
import com.academicshare.backend.group.service.GroupService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/groups")
    public ItemsResponse<GroupResponse> getMyGroups() {
        return new ItemsResponse<>(groupService.getMyGroups());
    }

    @PostMapping(value = "/groups", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public GroupCreateResponse createGroup(@RequestBody JsonNode request) {
        return groupService.createGroup(new GroupCreateRequest(textValue(request, "name")));
    }

    @PostMapping(value = "/groups/join", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public GroupMemberResponse joinGroup(@RequestBody JsonNode request) {
        return groupService.joinGroup(new GroupJoinRequest(textValue(request, "group_code")));
    }

    @GetMapping("/groups/{groupId}")
    public GroupDetailResponse getGroupDetail(@PathVariable Integer groupId) {
        return groupService.getGroupDetail(groupId);
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
}
