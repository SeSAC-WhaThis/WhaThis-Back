package com.example.whathis.product.dto.request;

import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 모든 필드는 선택적(Optional)
// null이 아닌 필드만 수정됨
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    private String title;

    private String description;
    
    // 종료일 (연장만 가능)
    private LocalDateTime endDate;

    @Positive(message = "카테고리 ID는 0보다 커야 합니다")
    private Long categoryId;

    private String thumbnailImageUrl;
    
    // ===== 수정 가능 여부 체크 =====
    // 제목이 있는가?
    public boolean hasTitle() {
        return title != null && !title.isBlank();
    }

    // 설명이 있는가?
    public boolean hasDescription() {
        return description != null && !description.isBlank();
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
        return thumbnailImageUrl != null;
    }

}
