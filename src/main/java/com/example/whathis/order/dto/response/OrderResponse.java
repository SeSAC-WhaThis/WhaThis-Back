package com.example.whathis.order.dto.response;

import com.example.whathis.common.order.OrderStatus;
import com.example.whathis.order.entity.Order;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderResponse {
    // 상품 정보
    private Long orderId;
    private String merchantUid;
    private String productName;
    private String productImageUrl;
    private LocalDateTime orderDate;
    private LocalDateTime endDate;

    // 구매 정보
    private Integer quantity;
    private BigDecimal totalAmount;
    private OrderStatus orderStatus;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String requestNote;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .merchantUid(order.getOrderNumber())
                .productName(order.getProduct().getTitle())
                .productImageUrl(order.getProduct().getThumbnailImageUrl())
                .orderDate(order.getReservedPaymentDate())
                .endDate(order.getProduct().getEndDate())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getStatus())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddress(order.getReceiverAddress())
                .requestNote(order.getRequest())
                .build();
    }
}
