package com.academicshare.backend.common.exception;

import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.error.ErrorResponse;
import com.academicshare.backend.common.error.ErrorResponseFactory;
import com.academicshare.backend.common.error.FieldErrorDetail;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ErrorResponseFactory errorResponseFactory;

    public GlobalExceptionHandler(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(errorResponseFactory.from(errorCode, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<FieldErrorDetail> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> errorResponseFactory.fieldError(error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(errorResponseFactory.validation(details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        List<FieldErrorDetail> details = exception.getConstraintViolations()
                .stream()
                .map(violation -> errorResponseFactory.fieldError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();

        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(errorResponseFactory.validation(details));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException exception) {
        List<FieldErrorDetail> details = exception.getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = error instanceof FieldError fieldError
                            ? fieldError.getField()
                            : "request";
                    return errorResponseFactory.fieldError(fieldName, error.getDefaultMessage());
                })
                .toList();

        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(errorResponseFactory.validation(details));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(errorResponseFactory.from(ErrorCode.VALIDATION_ERROR));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException exception) {
        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(errorResponseFactory.from(
                        ErrorCode.VALIDATION_ERROR,
                        "업로드 파일 크기가 허용 한도를 초과했습니다."
                ));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException exception) {
        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(errorResponseFactory.from(
                        ErrorCode.VALIDATION_ERROR,
                        "multipart/form-data 요청 형식이 올바르지 않습니다."
                ));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException exception) {
        ErrorCode errorCode = errorResponseFactory.fromStatus(exception.getStatusCode());

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(errorResponseFactory.from(errorCode, exception.getReason()));
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleErrorResponseException(ErrorResponseException exception) {
        ErrorCode errorCode = errorResponseFactory.fromStatus(exception.getStatusCode());
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(errorResponseFactory.from(
                        errorCode,
                        status == null ? errorCode.getDefaultMessage() : status.getReasonPhrase()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        log.error("Unhandled exception", exception);

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(errorResponseFactory.from(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
