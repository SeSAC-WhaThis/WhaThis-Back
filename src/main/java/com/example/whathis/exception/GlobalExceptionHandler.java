package com.example.whathis.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TYPE_MISMATCH_MESSAGE = "입력값이 올바르지 않습니다.";
    private static final String MISSING_PARAM_MESSAGE = "필수 파라미터가 누락되었습니다.";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(ApiException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("handleCustomException: {}", errorCode, e);

        Map<String, Object> body = Map.of(
            "message", errorCode.getMessage()
        );

        return ResponseEntity
            .status(errorCode.getStatus())
            .body(body);
    }
    
    // Spring Security 권한 에러 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("handleAccessDeniedException: {}", e.getMessage());

        Map<String, Object> body = Map.of(
            "message", "접근 권한이 없습니다."
        );

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(body);
    }

    // DTO Validation 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
        MethodArgumentNotValidException e) {

        Map<String, String> errors = e.getBindingResult() // 검증 결과
            .getFieldErrors() // 필드 에러만 가져옴
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField, // 필드 이름을 key로
                // 기본 메시지를 value로 (validation에 작성한 메시지)
                // 중복 시 기존 메시지
                FieldError::getDefaultMessage, (existingMsg, newMsg) -> existingMsg
            ));

        log.warn("Validation failed: {}", errors, e);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errors);
    }

    // 컨트롤러 파라미터 타입 예외
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(
        MethodArgumentTypeMismatchException e) {
        Map<String, String> errors = new HashMap<>();

        String fieldName = e.getName();

        errors.put(fieldName, TYPE_MISMATCH_MESSAGE);

        log.warn("Type mismatch: {} -> {}", fieldName, e.getValue());

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errors);
    }

    // 컨트롤러 파라미터 예외
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParams(
        MissingServletRequestParameterException e) {
        Map<String, String> errors = new HashMap<>();

        String missingParam = e.getParameterName();
        errors.put(missingParam, MISSING_PARAM_MESSAGE);

        log.warn("Missing Parameter: {}", missingParam);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errors);
    }

    // Todo 매핑되는 컨트롤러가 없을 때 등등 처리 필요
}
