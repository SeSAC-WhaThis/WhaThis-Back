package com.example.whathis.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 서버 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러가 발생했습니다."),

    // 유저(User)
    ADMIN_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "존재하지 않는 관리자입니다."),
    ADMIN_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "기존 비밀번호가 일치하지 않습니다."),
    ADMIN_LOGIN_PASSWORD_INCORRECT(HttpStatus.BAD_REQUEST, "비밀번호를 다시 입력해주세요."),
    ADMIN_PASSWORD_SAME(HttpStatus.BAD_REQUEST, "기존 비밀번호와 새 비밀번호가 동일합니다."),
    ADMIN_USER_ID_DUPLICATED(HttpStatus.CONFLICT, "존재하는 유저입니다."),
    ADMIN_ALREADY_LOGGED_OUT(HttpStatus.BAD_REQUEST, "이미 로그아웃된 유저입니다."),
    ADMIN_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 토큰(Token)
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다."),
    TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "지원하지 않는 JWT 토큰입니다."),
    TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "잘못된 형식의 JWT 토큰입니다."),
    TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "JWT 서명 검증에 실패했습니다."),
    TOKEN_ILLEGAL_ARGUMENT(HttpStatus.UNAUTHORIZED, "JWT 토큰이 비어있거나 잘못된 인자입니다."),
    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "블랙리스트에 등록된 토큰입니다."),
    TOKEN_ALREADY_LOGGED_OUT(HttpStatus.BAD_REQUEST, "이미 사용 완료된 토큰입니다."),
    TOKEN_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "토큰 타입이 일치하지 않습니다.");






    // 제품(Product)







    // 결제(Payment)







    // 리뷰(Review)






    // 카테고리(Category)






    // 재고(Inventory)






    private final HttpStatus status;
    private final String message;

}
