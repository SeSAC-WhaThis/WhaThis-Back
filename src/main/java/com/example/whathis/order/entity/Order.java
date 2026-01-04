package com.example.whathis.order.entity;

import com.example.whathis.BaseEntity;
import com.example.whathis.common.order.OrderStatus;
import com.example.whathis.product.entity.Product;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문 번호 (고유 식별자)
    // ex) ORD-20251214-A3B9C2F1
    @Column(nullable = false, unique = true)
    private String orderNumber;

    // 구매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    // 상품
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 주문 수량
    @Column(nullable = false)
    private Integer quantity;

    // 주문 금액
    @Column(nullable = false)
    private BigDecimal totalAmount;

    // 주문 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    // 예약 결제 정보
    private LocalDateTime reservedPaymentDate; // 펀딩 종료 시점에 실제 결제 예정

    // 주문 확정/취소 시간
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    // 주문 번호 자동 생성
    // ex) ORD-20251214-A3B9C2F1
    @PrePersist
    public void generateOrderNumber() {
        if (this.orderNumber == null) {
            this.orderNumber = "ORD-"
                    + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                    + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    @Column(nullable = false)
    private String receiverName;

    @Column(nullable = false)
    private String receiverPhone;

    @Column(nullable = false)
    private String receiverAddress;

    @Column
    private String request;

    @Builder
    public Order(
        User buyer, Product product, Integer quantity, BigDecimal totalAmount,
        OrderStatus status, LocalDateTime reservedPaymentDate, LocalDateTime confirmedAt,
        String  receiverName, String receiverPhone, String receiverAddress, String request
    ) {
        this.buyer = buyer;
        this.product = product;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.reservedPaymentDate = reservedPaymentDate;
        this.confirmedAt = confirmedAt;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.receiverAddress = receiverAddress;
        this.request = request;
    }

    // 주문 상태 예약으로 변경
    public void confirmOrder() {
        this.status = OrderStatus.RESERVED; // 펀딩은 '예약' 상태가 됨
        this.confirmedAt = LocalDateTime.now();
    }
}
