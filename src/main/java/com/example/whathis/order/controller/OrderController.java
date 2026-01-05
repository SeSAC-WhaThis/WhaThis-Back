package com.example.whathis.order.controller;

import com.example.whathis.common.response.ApiResponse;
import com.example.whathis.config.CustomUserDetails;
import com.example.whathis.order.dto.request.OrderCreateRequest;
import com.example.whathis.order.dto.response.OrderCreateResponse;
import com.example.whathis.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response = orderService.createOrder(userDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderCreateResponse>>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<OrderCreateResponse> responses = orderService.getOrderByUser(userDetails.getUser());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }
}
