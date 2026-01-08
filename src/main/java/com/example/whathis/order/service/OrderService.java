package com.example.whathis.order.service;

import com.example.whathis.common.exception.BusinessException;
import com.example.whathis.common.exception.ErrorCode;
import com.example.whathis.common.order.OrderStatus;
import com.example.whathis.order.dto.request.OrderCreateRequest;
import com.example.whathis.order.dto.response.OrderCreateResponse;
import com.example.whathis.order.dto.response.OrderResponse;
import com.example.whathis.order.dto.response.ProductBuyerResponse;
import com.example.whathis.order.entity.Order;
import com.example.whathis.order.repository.OrderRepository;
import com.example.whathis.product.entity.Product;
import com.example.whathis.product.repository.ProductRepository;
import com.example.whathis.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // 취소 사유가 펀딩 실패로 인한 취소인 것
    private static final String FUNDING_FAIL_REASON = "펀딩 실패로 인한 자동 환불";

    @Transactional
    public OrderCreateResponse createOrder(User buyer, OrderCreateRequest request) {
        // 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 본인이 등록한 상품인지 확인
        if(buyer.getId().equals(product.getSeller().getId())) {
            throw new BusinessException(ErrorCode.CANNOT_SELF_ORDER);
        }

        // 재고 확인
        if (!product.hasStock(request.getQuantity())) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }

        // 결제 금액 (quantity는 int라서 BigDecimal로 변환 후 계산)
        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        // PENDING(결제 대기) 상태 주문 생성
        Order order = Order.builder()
                .buyer(buyer)
                .product(product)
                .quantity(request.getQuantity())
                .totalAmount(totalAmount)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .receiverAddress(request.getReceiverAddress())
                .request(request.getRequestNote())
                .status(OrderStatus.PENDING)
                .build();

        orderRepository.save(order);

        return OrderCreateResponse.from(order);
    }

    public List<OrderResponse> getOrderByUser(User buyer) {
        List<Order> orders = orderRepository.findAllByBuyerOrderByIdDesc(buyer);

        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderByUserAndId(User buyer, Long orderId) {
        Order order = orderRepository.findByBuyerAndId(buyer, orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        return OrderResponse.from(order);
    }

    public List<ProductBuyerResponse> getProductBuyers(Long productId, User seller) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if(!product.getSeller().getId().equals(seller.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return orderRepository.findAllByProductIdAndSeller(
                productId, seller,
                        OrderStatus.PENDING,
                        OrderStatus.CANCELLED,
                        FUNDING_FAIL_REASON)
                .stream()
                .map(ProductBuyerResponse::from)
                .collect(Collectors.toList());
    }
}