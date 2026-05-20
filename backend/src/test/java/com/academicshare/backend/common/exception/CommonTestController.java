package com.academicshare.backend.common.exception;

import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.response.ApiResponse;
import com.academicshare.backend.common.response.ItemsResponse;
import com.academicshare.backend.common.response.PageResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestController
class CommonTestController {

    @GetMapping("/common-test/success")
    ApiResponse<TestUserResponse> success() {
        return ApiResponse.of(new TestUserResponse("user01"), "OK");
    }

    @GetMapping("/common-test/items")
    ItemsResponse<TestUserResponse> items() {
        return new ItemsResponse<>(List.of(new TestUserResponse("user01")));
    }

    @GetMapping("/common-test/page")
    PageResponse<TestUserResponse> page() {
        return new PageResponse<>(List.of(new TestUserResponse("user01")), 1, 10, 1, 1);
    }

    @PostMapping("/common-test/validation")
    ApiResponse<TestUserResponse> validate(@Valid @RequestBody TestUserRequest request) {
        return ApiResponse.of(new TestUserResponse(request.loginId()));
    }

    @GetMapping("/common-test/required-param")
    ApiResponse<String> requiredParam(@RequestParam String keyword) {
        return ApiResponse.of(keyword);
    }

    @GetMapping("/common-test/type-mismatch")
    ApiResponse<Integer> typeMismatch(@RequestParam Integer page) {
        return ApiResponse.of(page);
    }

    @GetMapping("/common-test/api-exception/{caseName}")
    void apiException(@PathVariable String caseName) {
        ErrorCode errorCode = switch (caseName) {
            case "unauthorized" -> ErrorCode.AUTHENTICATION_REQUIRED;
            case "forbidden" -> ErrorCode.ACCESS_DENIED;
            case "not-found" -> ErrorCode.RESOURCE_NOT_FOUND;
            case "conflict" -> ErrorCode.CONFLICT;
            default -> ErrorCode.INTERNAL_SERVER_ERROR;
        };

        throw new ApiException(errorCode);
    }

    @GetMapping("/common-test/max-upload-size")
    void maxUploadSize() {
        throw new MaxUploadSizeExceededException(1024);
    }
}

record TestUserRequest(
        @JsonProperty("login_id")
        @NotBlank
        String loginId
) {
}

record TestUserResponse(
        @JsonProperty("login_id")
        String loginId
) {
}
