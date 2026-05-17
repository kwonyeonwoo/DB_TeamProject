package com.academicshare.backend.user.controller;

import com.academicshare.backend.auth.dto.UserResponse;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.user.dto.UserUpdateRequest;
import com.academicshare.backend.user.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserResponse getMe() {
        return UserResponse.from(userService.getMe());
    }

    @PatchMapping
    public UserResponse updateMe(@RequestBody JsonNode request) {
        return UserResponse.from(userService.updateMe(new UserUpdateRequest(
                textValue(request, "name"),
                request.has("name"),
                textValue(request, "email_address"),
                request.has("email_address"),
                textValue(request, "current_password"),
                request.has("current_password"),
                textValue(request, "new_password"),
                request.has("new_password")
        )));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMe(HttpServletRequest request) {
        userService.deleteMe();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

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
}
