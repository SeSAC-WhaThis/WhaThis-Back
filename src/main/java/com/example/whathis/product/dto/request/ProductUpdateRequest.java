package com.example.whathis.product.dto.request;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

// 모든 필드는 선택적(Optional)
// null이 아닌 필드만 수정됨
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    private String title;

    private String description;

    private LocalDateTime startDate;

    // 종료일 (연장만 가능)
    private LocalDateTime endDate;

    @Positive(message = "카테고리 ID는 0보다 커야 합니다")
    private Long categoryId;

    private MultipartFile thumbnailImageUrl;

    private MultipartFile storyImageUrl;

    private String brn;

    @Positive(message = "가격은 0보다 커야 합니다.")
    private BigDecimal price;

    @Positive(message = "목표 금액은 0보다 커야 합니다.")
    private BigDecimal goalAmount;

    @Positive(message = "재고는 0보다 커야 합니다.")
    private Integer inventory;

    // ===== 수정 가능 여부 체크 =====
    // 제목이 있는가?
    public boolean hasTitle() {
        return title != null && !title.isBlank();
    }

    // 설명이 있는가?
    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }

    // 시작일자가 있는가?
    public boolean hasStartDate() {
        return startDate != null;
    }

    // 종료일자가 있는가?
    public boolean hasEndDate() {
        return endDate != null;
    }

    // 카테고리가 있는가?
    public boolean hasCategoryId() {
        return categoryId != null;
    }

    // 썸네일 이미지가 있는가?
    public boolean hasThumbnailImageUrl() {
        return thumbnailImageUrl != null && !thumbnailImageUrl.isEmpty();
    }

    // 스토리 이미지가 있는가?
    public boolean hasStoryImageUrl() {
        return storyImageUrl != null  && !storyImageUrl.isEmpty();
    }

    // 사업자 등록 번호가 있는가?
    public boolean hasBrn() {
        return brn != null && !brn.isBlank();
    }

    // 가격이 있는가?
    public boolean hasPrice() {
        return price != null;
    }

    // 목표 금액이 있는가?
    public boolean hasGoalAmount() {
        return goalAmount != null;
    }

    // 재고가 있는가?
    public boolean hasInventory() {
        return inventory != null;
    }

}
