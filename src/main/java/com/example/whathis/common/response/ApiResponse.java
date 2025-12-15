package com.example.whathis.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * API 공통 응답 형식
 * 
 * 성공 응답:
 * {
 *   "success": true,
 *   "data": { ... },
 *   "message": "성공 메시지"
 * }
 * 
 * 에러 응답:
 * {
 *   "success": false,
 *   "error": {
 *     "code": "ERROR_CODE",
 *     "message": "에러 메시지",
 *     "details": [ ... ]
 *   }
 * }
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private final boolean success;
    private final T data;
    private final String message;
    private final ErrorResponse error;
    
    // Private 생성자
    private ApiResponse(boolean success, T data, String message, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.error = error;
    }
    
    // 성공 응답 (데이터만)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }
    
    // 성공 응답 (데이터 + 메시지)
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }
    
    // 성공 응답 (메시지만)
    public static <T> ApiResponse<T> successWithMessage(String message) {
        return new ApiResponse<>(true, null, message, null);
    }
    
    // 에러 응답
    public static <T> ApiResponse<T> error(ErrorResponse error) {
        return new ApiResponse<>(false, null, null, error);
    }
    
    // 에러 응답 (간단한 메시지만)
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, null, ErrorResponse.of(code, message));
    }
}
