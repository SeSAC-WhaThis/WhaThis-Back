package com.example.whathis.common.exception;

import com.example.whathis.common.response.ApiResponse;
import com.example.whathis.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 전역 예외 처리 핸들러
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage(), e);

        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getCode(),
                e.getMessage());

        HttpStatus status = getHttpStatus(errorCode);

        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * Validation 예외 (@Valid 실패)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation error: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT.getCode(),
                ErrorCode.INVALID_INPUT.getMessage(),
                ErrorResponse.FieldError.of(e.getBindingResult().getFieldErrors()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * BindException 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e) {
        log.error("Bind error: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT.getCode(),
                ErrorCode.INVALID_INPUT.getMessage(),
                ErrorResponse.FieldError.of(e.getBindingResult().getFieldErrors()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT.getCode(),
                e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * 그 외 모든 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * 파일 업로드 용량 초과
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("MaxUploadSizeExceededException: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT.getCode(),
                "파일 업로드 용량이 초4과되었습니다.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * 지원하지 않는 미디어 타입 (JSON 대신 FormData 보낸 경우 등)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e) {
        log.error("HttpMediaTypeNotSupportedException: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT.getCode(),
                "지원하지 않는 미디어 타입입니다: " + e.getContentType());

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * ErrorCode에 따른 HTTP Status 매핑
     */
    private HttpStatus getHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_INPUT, INVALID_TYPE_VALUE, MISSING_INPUT_VALUE, PASSWORD_MISMATCH, PASSWORD_SAME_AS_OLD,
                 SAME_AS_CURRENT_NICKNAME, PAYMENT_AMOUNT_MISMATCH, PAYMENT_NOT_PAID ->
                HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN, ACCESS_DENIED, CANNOT_SELF_FOLLOW, CANNOT_SELF_ORDER -> HttpStatus.FORBIDDEN;
            case NOT_FOUND, USER_NOT_FOUND, PRODUCT_NOT_FOUND, ORDER_NOT_FOUND, PAYMENT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ALREADY_EXISTS, DUPLICATE_EMAIL, DUPLICATE_NICKNAME, ALREADY_LIKED, ALREADY_FOLLOWING, NOT_FOLLOWING,
                    OUT_OF_STOCK ->
                HttpStatus.CONFLICT;
            case FUNDING_NOT_ONGOING, FUNDING_ENDED, INVALID_ORDER_STATUS, CANNOT_CANCEL_ORDER ->
                HttpStatus.UNPROCESSABLE_ENTITY;
            case OAUTH_TOKEN_FAILED, OAUTH_USER_INFO_FAILED, PAYMENT_FETCH_FAILED, PAYMENT_CANCEL_FAILED, PG_CONNECT_ERROR -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

}
