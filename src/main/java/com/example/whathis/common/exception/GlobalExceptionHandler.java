package com.example.whathis.common.exception;

import com.example.whathis.common.response.ApiResponse;
import com.example.whathis.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
            e.getMessage()
        );
        
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
            ErrorResponse.FieldError.of(e.getBindingResult().getFieldErrors())
        );
        
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
            ErrorResponse.FieldError.of(e.getBindingResult().getFieldErrors())
        );
        
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
            e.getMessage()
        );
        
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
            ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(errorResponse));
    }
    
    /**
     * ErrorCode에 따른 HTTP Status 매핑
     */
    private HttpStatus getHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_INPUT, INVALID_TYPE_VALUE, MISSING_INPUT_VALUE, PASSWORD_MISMATCH, PASSWORD_SAME_AS_OLD -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN, ACCESS_DENIED -> HttpStatus.FORBIDDEN;
            case NOT_FOUND, USER_NOT_FOUND, PRODUCT_NOT_FOUND, ORDER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ALREADY_EXISTS, DUPLICATE_EMAIL, DUPLICATE_NICKNAME, ALREADY_LIKED, ALREADY_FOLLOWING, OUT_OF_STOCK -> HttpStatus.CONFLICT;
            case FUNDING_NOT_ONGOING, FUNDING_ENDED, INVALID_ORDER_STATUS, CANNOT_CANCEL_ORDER -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

}
