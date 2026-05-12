package com.academicshare.backend.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void errorResponseContainsCodeAndMessage() throws Exception {
        ErrorResponse response = ErrorResponse.of(ErrorCode.VALIDATION_ERROR);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("code");
        assertThat(json).contains("message");
        assertThat(json).doesNotContain("details");
    }

    @Test
    void errorResponseCanContainDetails() throws Exception {
        ErrorResponse response = ErrorResponse.validation(List.of(
                new FieldErrorDetail("login_id", "필수 입력값입니다.")
        ));

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("details");
        assertThat(json).contains("login_id");
    }
}
