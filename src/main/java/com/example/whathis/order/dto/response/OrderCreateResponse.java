package com.example.whathis.order.dto.response;

import com.example.whathis.order.entity.Order;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderCreateResponse {
    private Long orderId;
    private String merchantUid; // 주문 번호
    private String productName;
    private BigDecimal amount;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String requestNote;

    public static OrderCreateResponse from(Order order) {
        return OrderCreateResponse.builder()
                .orderId(order.getId())
                .merchantUid(order.getOrderNumber())
                .productName(order.getProduct().getTitle())
                .amount(order.getTotalAmount())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddress(order.getReceiverAddress())
                .requestNote(order.getRequest())
                .build();


    }
}
