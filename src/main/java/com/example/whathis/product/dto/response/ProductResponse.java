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

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

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
    private UserResponse seller;
    private CategoryResponse category;
    private Long daysLeft;
    private LocalDateTime createdAt;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
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
                .thumbnailImageUrl(product.getThumbnailImageUrl())
                .seller(UserResponse.from(product.getSeller()))
                .category(CategoryResponse.from(product.getCategory()))
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
