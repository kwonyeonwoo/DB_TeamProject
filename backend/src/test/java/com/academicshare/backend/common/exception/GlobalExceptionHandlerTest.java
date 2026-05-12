package com.academicshare.backend.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.error.ErrorResponseFactory;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CommonTestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, ErrorResponseFactory.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiResponseReturnsDataAndMessage() throws Exception {
        mockMvc.perform(get("/common-test/success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.login_id").value("user01"))
                .andExpect(jsonPath("$.message").value("OK"));
    }

    @Test
    void itemsResponseReturnsItems() throws Exception {
        mockMvc.perform(get("/common-test/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].login_id").value("user01"));
    }

    @Test
    void pageResponseUsesApiFieldNames() throws Exception {
        mockMvc.perform(get("/common-test/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].login_id").value("user01"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.total_count").value(1))
                .andExpect(jsonPath("$.total_pages").value(1));
    }

    @Test
    void validationErrorReturnsCodeMessageAndDetails() throws Exception {
        mockMvc.perform(post("/common-test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.details[0].field").value("login_id"))
                .andExpect(jsonPath("$.details[0].message").isNotEmpty());
    }

    @Test
    void malformedJsonReturnsValidationErrorWithoutDetails() throws Exception {
        mockMvc.perform(post("/common-test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void missingRequestParamReturnsValidationError() throws Exception {
        mockMvc.perform(get("/common-test/required-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void typeMismatchReturnsValidationError() throws Exception {
        mockMvc.perform(get("/common-test/type-mismatch").param("page", "not-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void apiExceptionReturnsConfiguredStatusAndErrorBody() throws Exception {
        Map<String, ErrorCode> cases = Map.of(
                "unauthorized", ErrorCode.AUTHENTICATION_REQUIRED,
                "forbidden", ErrorCode.ACCESS_DENIED,
                "not-found", ErrorCode.RESOURCE_NOT_FOUND,
                "conflict", ErrorCode.CONFLICT
        );

        for (Map.Entry<String, ErrorCode> entry : cases.entrySet()) {
            mockMvc.perform(get("/common-test/api-exception/{caseName}", entry.getKey()))
                    .andExpect(status().is(entry.getValue().getStatus().value()))
                    .andExpect(jsonPath("$.code").value(entry.getValue().name()))
                    .andExpect(jsonPath("$.message").value(entry.getValue().getDefaultMessage()))
                    .andExpect(jsonPath("$.details").doesNotExist());
        }
    }

}
