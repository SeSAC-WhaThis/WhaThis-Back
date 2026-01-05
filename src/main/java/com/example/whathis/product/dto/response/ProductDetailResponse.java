package com.example.whathis.product.dto.response;

import com.example.whathis.category.dto.response.CategoryResponse;
import com.example.whathis.product.entity.Product;
import com.example.whathis.user.dto.response.UserResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 펀딩 상품 상세 조회 응답 DTO
// 단일 조회 시 사용할 반환 객체
// 목록 조회보다 더 많은 정보 포함
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal goalAmount;
    private BigDecimal currentAmount;
    private Integer buyerCount;
    private Double achievementRate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer viewCount;
    private String thumbnailImageUrl;
    private String storyImageUrl;
    private UserResponse seller;
    private CategoryResponse category;
    private Integer inventory; // 재고 수량
    private Boolean isLowStock; // 마감 임박 여부
    private Boolean isOutOfStock; // 품절 여부
    private Long likeCount;
    private Boolean isLiked;
    private Long daysLeft;
    private LocalDateTime createdAt;

    // Entity -> DTO 변환
    public static ProductDetailResponse from(
            Product product,
            Long likeCount,
            Boolean isLiked
    ) {
        return ProductDetailResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .goalAmount(product.getGoalAmount())
                .currentAmount(product.getCurrentAmount())
                .buyerCount(product.getBuyerCount())
                .achievementRate(product.getAchievementRate())
                .startDate(product.getStartDate())
                .endDate(product.getEndDate())

                .viewCount(product.getViewCount())
                .thumbnailImageUrl(product.getThumbnailImageUrl() != null
                        ? "http://localhost:8080" + product.getThumbnailImageUrl()
                        : null)
                .storyImageUrl(product.getStoryImageUrl() != null
                        ? "http://localhost:8080" + product.getStoryImageUrl()
                        : null)
                .seller(UserResponse.from(product.getSeller()))
                .category(CategoryResponse.from(product.getCategory()))
                .inventory(product.getInventory())
                .isLowStock(product.isLowStock())
                .isOutOfStock(product.isOutOfStock())
                .likeCount(likeCount)
                .isLiked(isLiked)
                .daysLeft(calculateDaysLeft(product.getEndDate()))
                .createdAt(product.getCreatedAt())
                .build();
    }

    // 남은 일수 계산
    private static Long calculateDaysLeft(LocalDateTime endDate) {
        if (endDate == null) {
            return null;
        }

        long days = ChronoUnit.DAYS.between(LocalDateTime.now(), endDate);
        return days >= 0 ? days : 0;
    }

}
