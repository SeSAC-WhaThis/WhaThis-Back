package com.example.whathis.product.dto.response;

import com.example.whathis.category.entity.Category;
import com.example.whathis.common.product.ProductStatus;
import com.example.whathis.product.entity.Product;
import com.example.whathis.user.entity.User;
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
    private ProductStatus status;
    private Integer viewCount;
    private String thumbnailImageUrl;
    private SellerInfo seller;
    private CategoryInfo category;
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
                .status(product.getStatus())
                .viewCount(product.getViewCount())
                .thumbnailImageUrl(product.getThumbnailImageUrl())
                .seller(SellerInfo.from(product.getSeller()))
                .category(CategoryInfo.from(product.getCategory()))
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
    
    // 판매자 정보(중첩 클래스)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerInfo {
        private Long id;
        private String name;
        private String profileImageUrl;
        
        public static SellerInfo from(User user) {
            if (user == null) {
                return null;
            }
            return SellerInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
        }
    }
    
    // 카테고리 정보(중첩 클래스)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
        
        public static CategoryInfo from(Category category) {
            if (category == null) {
                return null;
            }
            return CategoryInfo.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
        }
    }

}
