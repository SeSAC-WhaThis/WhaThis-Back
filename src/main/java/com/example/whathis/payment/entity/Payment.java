package com.example.whathis.payment.entity;

import com.example.whathis.BaseEntity;
import com.example.whathis.common.payment.PaymentMethod;
import com.example.whathis.common.payment.PaymentStatus;
import com.example.whathis.common.payment.PaymentType;
import com.example.whathis.order.entity.Order;
import com.example.whathis.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "payments")
@NoArgsConstructor
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 결제 번호 (고유 식별자)
    // ex) PAY-20251214-B5C3E9A2
    @Column(nullable = false, unique = true)
    private String paymentNumber;

    // 주문 정보
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 결제자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 결제 금액
    @Column(nullable = false)
    private BigDecimal amount;

    // 결제 방법
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    // 결제 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    // 결제 타입 (펀딩은 예약 결제)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type = PaymentType.RESERVED;

    // PG사 관련 정보
    private String pgProvider;      // 결제 대행사 (토스, 카카오페이 등)
    private String pgTransactionId; // PG사 거래 ID
    private String pgApprovalNumber; // 승인 번호

    // 결제 시간
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime refundedAt;

    // 취소/환불 사유
    private String cancellationReason;
    private String refundReason;

    // 결제 번호 자동 생성
    // ex) PAY-20251214-B5C3E9A2
    @PrePersist
    public void generatePaymentNumber() {
        this.paymentNumber = "PAY-"
            + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
            + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
