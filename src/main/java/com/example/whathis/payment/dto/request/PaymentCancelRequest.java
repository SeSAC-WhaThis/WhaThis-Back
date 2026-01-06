package com.example.whathis.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentCancelRequest {
    // 결제ID(주문번호)
    @NotBlank(message = "결제 아이디는 필수입니다.")
    private String paymentId;

    @NotBlank(message = "취소 사유는 필수입니다.")
    private String cancelReason;
}
