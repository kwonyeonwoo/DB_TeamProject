package com.academicshare.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank
        @Size(max = 50)
        String loginId,

        @NotBlank
        @Size(max = 255)
        String password,

        @NotBlank
        @Size(max = 50)
        String name,

        @NotBlank
        @Email
        @Size(max = 255)
        String emailAddress
) {
}
