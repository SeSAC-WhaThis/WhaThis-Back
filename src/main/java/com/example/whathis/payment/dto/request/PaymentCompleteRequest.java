package com.example.whathis.payment.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentCompleteRequest {
    private String paymentId;
}