package com.example.whathis.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * API 에러 응답 형식
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private final String code;
    private final String message;
    private final List<FieldError> details;
    
    private ErrorResponse(String code, String message, List<FieldError> details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }
    
    // 기본 에러 응답
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null);
    }
    
    // 필드 에러 포함 (Validation 에러용)
    public static ErrorResponse of(String code, String message, List<FieldError> details) {
        return new ErrorResponse(code, message, details);
    }
    
    /**
     * 필드 에러 정보
     * Validation 실패 시 어떤 필드가 문제인지 상세 정보 제공
     */
    @Getter
    public static class FieldError {
        private final String field;
        private final String value;
        private final String reason;
        
        private FieldError(String field, String value, String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }
        
        public static FieldError of(String field, String value, String reason) {
            return new FieldError(field, value, reason);
        }
        
        // Spring Validation의 FieldError를 변환
        public static List<FieldError> of(List<org.springframework.validation.FieldError> errors) {
            List<FieldError> fieldErrors = new ArrayList<>();
            for (org.springframework.validation.FieldError error : errors) {
                fieldErrors.add(FieldError.of(
                    error.getField(),
                    error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                    error.getDefaultMessage()
                ));
            }
            return fieldErrors;
        }
    }
}


