package com.example.whathis.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    // 사업자 등록 번호
    // 최초 상품 등록 시에만 필수, 이미 등록된 사용자는 생략 가능
    // ex) 123-45-67890
    @NotNull(message = "사업자등록번호는 필수입니다.")
    @Pattern(
        regexp = "^(\\d{3})-?(\\d{2})-?(\\d{5})$",
        message = "사업자등록번호 형식이 올바르지 않습니다."
    )
    private String brn;

    @NotBlank(message = "제품 사진은 필수입니다.")
    @Pattern(
        regexp = "^(https?://.*\\.(png|jpg|jpeg|webp))$",
        message = "이미지 URL(jpg, jpeg, png, webp) 형식이어야 합니다."
    )
    private String thumbnailImageUrl;
    
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 40, message = "제목은 40자 이내로 입력해야 합니다.")
    private String title;
    
    @NotBlank(message = "설명은 필수입니다.")
    @Size(max = 500, message = "설명은 500자 이내로 입력해야 합니다.")
    private String description;
    
    @NotNull(message = "가격은 필수입니다.")
    @Positive(message = "가격은 0보다 커야 합니다.")
    private BigDecimal price;
    
    @NotNull(message = "목표 금액은 필수입니다.")
    @Positive(message = "목표 금액은 0보다 커야 합니다.")
    private BigDecimal goalAmount;
    
    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;
    
    @NotNull(message = "시작일은 필수입니다.")
    private LocalDateTime startDate;
    
    @NotNull(message = "종료일은 필수입니다.")
    private LocalDateTime endDate;
    
    @NotNull(message = "재고 수량은 필수입니다.")
    @Positive(message = "재고는 0보다 커야 합니다.")
    private Integer inventory;

}
