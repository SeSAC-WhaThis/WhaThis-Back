package com.example.whathis.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId;

    @NotNull(message = "상품 수량은 필수입니다")
    private Integer quantity;

    // 배송 정보
    @NotBlank(message = "수령인 이름은 필수입니다.")
    private String receiverName;

    @NotBlank(message = "수령인 연락처는 필수입니다.")
    private String receiverPhone;

    @NotBlank(message = "주소는 필수입니다.")
    private String receiverAddress;

    private String requestNote;

}
