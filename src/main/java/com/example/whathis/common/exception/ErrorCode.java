package com.example.whathis.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // 400 Bad Request
    INVALID_INPUT("INVALID_INPUT", "잘못된 입력 값입니다"),
    INVALID_TYPE_VALUE("INVALID_TYPE_VALUE", "잘못된 타입입니다"),
    MISSING_INPUT_VALUE("MISSING_INPUT_VALUE", "필수 값이 누락되었습니다"),
    PASSWORD_MISMATCH("PASSWORD_MISMATCH", "새 비밀번호가 일치하지 않습니다"),
    PASSWORD_SAME_AS_OLD("PASSWORD_SAME_AS_OLD", "기존 비밀번호와 동일합니다"),
    SAME_AS_CURRENT_NICKNAME("SAME_AS_CURRENT_NICKNAME", "기존 닉네임과 동일합니다."),
    
    // 401 Unauthorized
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다"),
    INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN("EXPIRED_TOKEN", "만료된 토큰입니다"),
    INVALID_PASSWORD("INVALID_PASSWORD", "비밀번호가 일치하지 않습니다"),
    
    // 403 Forbidden
    FORBIDDEN("FORBIDDEN", "권한이 없습니다"),
    ACCESS_DENIED("ACCESS_DENIED", "접근이 거부되었습니다"),
    CANNOT_SELF_LIKE("CANNOT_SELF_LIKE", "본인의 제품에는 좋아요를 누를 수 없습니다."),
    
    // 404 Not Found
    NOT_FOUND("NOT_FOUND", "리소스를 찾을 수 없습니다"),
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다"),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다"),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다"),
    REVIEW_NOT_FOUND("REVIEW_NOT_FOUND", "리뷰를 찾을 수 없습니다"),
    
    // 409 Conflict
    ALREADY_EXISTS("ALREADY_EXISTS", "이미 존재합니다"),
    DUPLICATE_EMAIL("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다"),
    DUPLICATE_NICKNAME("DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다."),
    ALREADY_LIKED("ALREADY_LIKED", "이미 좋아요한 상품입니다"),
    NOT_LIKED("NOT_LIKED", "좋아요하지 않은 게시물입니다"),
    ALREADY_FOLLOWING("ALREADY_FOLLOWING", "이미 팔로우 중입니다"),
    OUT_OF_STOCK("OUT_OF_STOCK", "재고가 부족합니다"),
    ALREADY_REVIEWED("ALREADY_REVIEWED", "이미 리뷰를 작성한 주문입니다"),
    
    // 422 Unprocessable Entity
    FUNDING_NOT_ONGOING("FUNDING_NOT_ONGOING", "펀딩이 진행 중이 아닙니다"),
    FUNDING_ENDED("FUNDING_ENDED", "펀딩이 종료되었습니다"),
    INVALID_ORDER_STATUS("INVALID_ORDER_STATUS", "주문 상태가 올바르지 않습니다"),
    CANNOT_CANCEL_ORDER("CANNOT_CANCEL_ORDER", "주문을 취소할 수 없습니다"),
    NOT_YOUR_ORDER("NOT_YOUR_ORDER", "본인의 주문이 아닙니다"),
    NOT_YOUR_REVIEW("NOT_YOUR_REVIEW", "본인의 리뷰가 아닙니다"),
    ORDER_NOT_CONFIRMED("ORDER_NOT_CONFIRMED", "주문이 확정되지 않았습니다"),
    
    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다"),
    OAUTH_TOKEN_FAILED("OAUTH_TOKEN_FAILED", "카카오 토큰 요청에 실패했습니다"),
    OAUTH_USER_INFO_FAILED("OAUTH_USER_INFO_FAILED", "카카오 사용자 정보 요청에 실패했습니다");
    
    private final String code;
    private final String message;

}
