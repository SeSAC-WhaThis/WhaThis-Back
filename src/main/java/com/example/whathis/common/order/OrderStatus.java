package com.example.whathis.common.order;

public enum OrderStatus {
    PENDING,          // 대기중 (예약 결제 대기)
    RESERVED,         // 예약 완료 (펀딩 참여 완료)
    CONFIRMED,        // 주문 확정 (리뷰 작성 가능 상태)
    CANCELLED,        // 취소 (펀딩 실패 or 사용자 취소)
}
