package com.example.whathis.order.dto.response;

import com.example.whathis.common.order.OrderStatus;
import com.example.whathis.order.entity.Order;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductBuyerResponse {
    private Long orderId;

    // 구매자 주문 정보
    private String buyerNickname;
    private String buyerProfileImage;
    private int quantity;
    private BigDecimal totalAmount;

    public static ProductBuyerResponse from(Order order) {
        return ProductBuyerResponse.builder()
                .orderId(order.getId())
                .buyerNickname(order.getBuyer().getNickname())
                .buyerProfileImage(order.getBuyer().getProfileImageUrl())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .build();
    }
}
