package com.example.whathis.payment.controller;

import com.example.whathis.common.response.ApiResponse;
import com.example.whathis.config.CustomUserDetails;
import com.example.whathis.payment.dto.request.PaymentCompleteRequest;
import com.example.whathis.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<Void>> completePayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PaymentCompleteRequest request
    ) {
        paymentService.verifyAndCompletePayment(userDetails.getUser(), request);
        return ResponseEntity.ok(ApiResponse.successWithMessage("결제가 성공적으로 완료되었습니다."));
    }
}
