package com.example.whathis.payment.service;

import com.example.whathis.common.exception.BusinessException;
import com.example.whathis.common.exception.ErrorCode;
import com.example.whathis.common.payment.PaymentMethod;
import com.example.whathis.common.payment.PaymentStatus;
import com.example.whathis.common.payment.PaymentType;
import com.example.whathis.order.entity.Order;
import com.example.whathis.order.repository.OrderRepository;
import com.example.whathis.payment.dto.request.PaymentCompleteRequest;
import com.example.whathis.payment.entity.Payment;
import com.example.whathis.payment.repository.PaymentRepository;
import com.example.whathis.user.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "결제 정보 조회 실패");
        }

        // V2 응답 구조: { "id": "...", "status": "PAID", "amount": { "total": 1000, ... }, ... }
        JsonNode paymentData = paymentResponse;

        // 주문 조회 (paymentId = merchantUid = orderNumber)
        Order order = orderRepository.findByOrderNumber(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 결제 금액 검증
        BigDecimal paidAmount = new BigDecimal(paymentData.get("amount").get("total").asText());
        if (order.getTotalAmount().compareTo(paidAmount) != 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "결제 금액 불일치");
        }

        // 결제 상태 검증
        String status = paymentData.get("status").asText();
        if (!"PAID".equals(status)) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "결제가 완료되지 않았습니다.");
        }

        // 주문 상태 변경 및 저장
        order.confirmOrder();
        order.getProduct().decreaseInventory(order.getQuantity());

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

}