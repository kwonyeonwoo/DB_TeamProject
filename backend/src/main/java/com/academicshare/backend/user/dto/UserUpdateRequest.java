package com.academicshare.backend.user.dto;

public record UserUpdateRequest(
        String name,
        boolean nameProvided,
        String emailAddress,
        boolean emailAddressProvided,
        String currentPassword,
        boolean currentPasswordProvided,
        String newPassword,
        boolean newPasswordProvided
) {

    public boolean hasAnyUpdateField() {
        return nameProvided || emailAddressProvided || newPasswordProvided;
    }
}
