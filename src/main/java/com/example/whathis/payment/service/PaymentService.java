package com.example.whathis.payment.service;

import com.example.whathis.common.exception.BusinessException;
import com.example.whathis.common.exception.ErrorCode;
import com.example.whathis.common.order.OrderStatus;
import com.example.whathis.common.payment.PaymentMethod;
import com.example.whathis.common.payment.PaymentStatus;
import com.example.whathis.common.payment.PaymentType;
import com.example.whathis.order.entity.Order;
import com.example.whathis.order.repository.OrderRepository;
import com.example.whathis.payment.dto.request.PaymentCancelRequest;
import com.example.whathis.payment.dto.request.PaymentCompleteRequest;
import com.example.whathis.payment.entity.Payment;
import com.example.whathis.payment.repository.PaymentRepository;
import com.example.whathis.user.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final WebClient webClient = WebClient.create("https://api.portone.io"); // V2 API 주소

    @Value("${portone.api-secret}")
    private String apiSecret;

    // 결제 검증 및 완료 처리
    public void verifyAndCompletePayment(User user, PaymentCompleteRequest request) {
        String paymentId = request.getPaymentId();

        // 포트원 결제내역 조회
        // 토큰 발급 없이 Secret Key를 헤더에 바로 사용
        JsonNode paymentResponse = webClient.get()
                .uri("/payments/" + paymentId)
                .header("Authorization", "PortOne " + apiSecret)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (paymentResponse == null || paymentResponse.has("code")) {
            throw new BusinessException(ErrorCode.PAYMENT_FETCH_FAILED);
        }

        // V2 응답 구조: { "id": "...", "status": "PAID", "amount": { "total": 1000, ... }, ... }
        JsonNode paymentData = paymentResponse;

        // 주문 조회 (paymentId = merchantUid = orderNumber)
        Order order = orderRepository.findByOrderNumber(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 결제 금액 검증
        BigDecimal paidAmount = new BigDecimal(paymentData.get("amount").get("total").asText());
        if (order.getTotalAmount().compareTo(paidAmount) != 0) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 결제 상태 검증
        String status = paymentData.get("status").asText();
        if (!"PAID".equals(status)) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PAID);
        }

        // 주문 상태 변경 및 저장
        order.confirmOrder();
        order.getProduct().decreaseInventoryAndIncreaseCurrentAmount(order.getQuantity());

        Payment payment = Payment.builder()
                .order(order)
                .user(user)
                .amount(paidAmount)
                .method(PaymentMethod.CREDIT_CARD) // V2 응답에서 method 파싱 필요
                .status(PaymentStatus.APPROVED)
                .type(PaymentType.RESERVED)
                .pgProvider("PORTONE_V2")
                .pgTransactionId(paymentData.get("id").asText()) // paymentId
                .paidAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);
    }

    public void cancelPayment(User user, PaymentCancelRequest request) {
        String paymentId = request.getPaymentId();

        // 주문 및 결제 정보 조회
        Order order = orderRepository.findByOrderNumber(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if(!order.getBuyer().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NOT_YOUR_ORDER);
        }

        if(order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_ORDER);
        }

        processRefund(order, request.getCancelReason()); // 공통 메서드 호출
    }

    // 스케줄러(ProductScheduler)에 의한 강제 환불
    public void refundBySystem(Order order) {
        Order managedOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // RESERVED 상태가 아니라면 환불 처리를 건너뜀
        if (managedOrder.getStatus() != OrderStatus.RESERVED) {
            return;
        }
        processRefund(managedOrder, "펀딩 실패로 인한 자동 환불");
    }

    // 환불 공통 로직
    private void processRefund(Order order, String reason) {
        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 포트원 API 환불 요청
        try {
            webClient.post()
                    .uri("/payments/" + order.getOrderNumber() + "/cancel")
                    .header("Authorization", "PortOne " + apiSecret)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("reason", reason))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PAYMENT_CANCEL_FAILED);
        }

        // 주문과 결제 상태 CANCELLED로 변경
        order.cancelOrder(reason);
        payment.cancelPayment(reason);

        // 상품 재고 및 현재 금액 복구
        order.getProduct().increaseInventoryAndDecreaseCurrentAmount(order.getQuantity());
    }

    // 판매자 탈퇴 시 결제완료인 주문 일괄 환불
    public void refundAllForSellerWithdrawal(User seller) {
        // 환불 대상 상태 : RESERVED, CONFIRMED
        List<OrderStatus> activeStatuses = Arrays.asList(OrderStatus.RESERVED, OrderStatus.CONFIRMED);

        // 해당 판매자의 상품에 대한 유효 주문 조회
        List<Order> activeOrders = orderRepository.findAllByProductSellerAndStatusIn(seller, activeStatuses);

        // 하나씩 환불 처리
        for (Order order : activeOrders) {
            processRefund(order, "판매자 탈퇴로 인한 펀딩 취소 및 자동 환불");
        }
    }
}