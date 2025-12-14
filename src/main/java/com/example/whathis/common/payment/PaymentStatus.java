package com.example.whathis.common.payment;

public enum PaymentStatus {

    PENDING,    // 대기중
    APPROVED,   // 승인 완료
    FAILED,     // 실패
    CANCELLED,  // 취소
    REFUNDED    // 환불
}
