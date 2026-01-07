package com.example.whathis.order.controller;

import com.example.whathis.common.response.ApiResponse;
import com.example.whathis.config.CustomUserDetails;
import com.example.whathis.order.dto.request.OrderCreateRequest;
import com.example.whathis.order.dto.response.OrderCreateResponse;
import com.example.whathis.order.dto.response.OrderResponse;
import com.example.whathis.order.service.OrderService;
import com.example.whathis.payment.dto.request.PaymentCancelRequest;
import com.example.whathis.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response = orderService.createOrder(userDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<OrderResponse> responses = orderService.getOrderByUser(userDetails.getUser());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId
    ) {
        OrderResponse response = orderService.getOrderByUserAndId(userDetails.getUser(), orderId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PaymentCancelRequest request
    ) {
        paymentService.cancelPayment(userDetails.getUser(), request);
        return ResponseEntity.ok(ApiResponse.successWithMessage("주문 취소가 완료되었습니다."));
    }
}
