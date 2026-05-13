package com.academicshare.backend.common.error;

import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

@Service
public class ErrorResponseFactory {

    public ErrorResponse from(ErrorCode errorCode) {
        return ErrorResponse.of(errorCode);
    }

    public ErrorResponse from(ErrorCode errorCode, String message) {
        return ErrorResponse.of(errorCode, resolveMessage(errorCode, message));
    }

    public ErrorResponse validation(List<FieldErrorDetail> details) {
        if (details == null || details.isEmpty()) {
            return ErrorResponse.of(ErrorCode.VALIDATION_ERROR);
        }

        return ErrorResponse.validation(details);
    }

    public FieldErrorDetail fieldError(String field, String message) {
        return new FieldErrorDetail(toApiFieldName(field), resolveMessage(ErrorCode.VALIDATION_ERROR, message));
    }

    public ErrorCode fromStatus(HttpStatusCode statusCode) {
        return switch (statusCode.value()) {
            case 400 -> ErrorCode.VALIDATION_ERROR;
            case 401 -> ErrorCode.AUTHENTICATION_REQUIRED;
            case 403 -> ErrorCode.ACCESS_DENIED;
            case 404 -> ErrorCode.RESOURCE_NOT_FOUND;
            case 409 -> ErrorCode.CONFLICT;
            default -> ErrorCode.INTERNAL_SERVER_ERROR;
        };
    }

    private String resolveMessage(ErrorCode errorCode, String message) {
        return message == null || message.isBlank()
                ? errorCode.getDefaultMessage()
                : message;
    }

    private String toApiFieldName(String field) {
        if (field == null || field.isBlank()) {
            return field;
        }

        String fieldName = field;
        int lastDotIndex = fieldName.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < fieldName.length() - 1) {
            fieldName = fieldName.substring(lastDotIndex + 1);
        }

        return fieldName
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT);
    }
}
