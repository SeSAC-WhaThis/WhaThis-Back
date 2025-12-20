package com.example.whathis.product.entity;

import com.example.whathis.BaseEntity;
import com.example.whathis.category.entity.Category;
import com.example.whathis.common.product.ProductStatus;
import com.example.whathis.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "products")
@NoArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // 판매자 (프로젝트 오픈한 사람)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    // 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // 가격 정보
    @Column(nullable = false)
    private BigDecimal price;

    // 재고 수량
    @Column(nullable = false)
    @Min(0)
    private Integer inventory = 0;

    // 펀딩 목표 금액
    @Column(nullable = false)
    private BigDecimal goalAmount;

    // 현재 펀딩 금액
    @Column(nullable = false)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    // 펀딩 참여자 수
    @Column(nullable = false)
    private Integer buyerCount = 0;

    // 펀딩 기간
    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    // 상품에 대한 펀딩 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.PREPARING;

    // 조회 수
    @Column(nullable = false)
    private Integer viewCount = 0;

    // 대표 이미지
    @Column(nullable = false)
    private String thumbnailImageUrl;

    // 정적 팩토리 메서드
    public static Product of(
        String title, String description, User seller, Category category, BigDecimal price,
        BigDecimal goalAmount, LocalDateTime startDate, LocalDateTime endDate, 
        String thumbnailImageUrl, Integer inventory
    ) {
        Product product = new Product();
        product.title = title;
        product.description = description;
        product.seller = seller;
        product.category = category;
        product.price = price;
        product.goalAmount = goalAmount;
        product.startDate = startDate;
        product.endDate = endDate;
        product.thumbnailImageUrl = thumbnailImageUrl;
        product.inventory = inventory;
        product.status = ProductStatus.PREPARING;
        product.currentAmount = BigDecimal.ZERO;
        product.buyerCount = 0;
        product.viewCount = 0;
        return product;
    }
    
    // ========== 아래부터는 비즈니스 메서드 ==========
    
    // 조회수 증가
    public void increaseViewCount() {
        this.viewCount++;
    }
    
    // 펀딩 참여 (currentAmount, buyerCount 증가)
    public void participate(BigDecimal amount) {
        this.currentAmount = this.currentAmount.add(amount);
        this.buyerCount++;
    }
    
    // 달성률 계산
    public double getAchievementRate() {
        if (goalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        return currentAmount.divide(goalAmount, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
    
    // 펀딩 진행 중인지 확인
    public boolean isOngoing() {
        return status == ProductStatus.ONGOING;
    }
    
    // 펀딩 성공 여부 확인
    public boolean isSuccess() {
        return currentAmount.compareTo(goalAmount) >= 0;
    }
    
    // 상품 정보 수정 (선택적 업데이트)
    public void update(
        String title, String description, Category category,
        LocalDateTime endDate, String thumbnailImageUrl
    ) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }

        if (description != null && !description.isBlank()) {
            this.description = description;
        }

        if (category != null) {
            this.category = category;
        }

        if (endDate != null) {
            // 종료일은 연장만 가능 (단축 불가)
            if (endDate.isBefore(this.endDate)) {
                throw new IllegalArgumentException(
                    "종료일은 연장만 가능합니다. 현재 종료일: " + this.endDate
                );
            }
            this.endDate = endDate;
        }

        if (thumbnailImageUrl != null) {
            this.thumbnailImageUrl = thumbnailImageUrl;
        }
    }
    
    // 판매자 확인
    public boolean isOwnedBy(User user) {
        return this.seller.getId().equals(user.getId());
    }
    
    // 재고 감소 (펀딩 참여 시)
    public void decreaseInventory(int quantity) {
        if (this.inventory < quantity) {
            throw new IllegalStateException("재고가 부족합니다. 남은 재고: " + this.inventory);
        }
        this.inventory -= quantity;
    }
    
    // 재고 증가 (펀딩 취소 시)
    public void increaseInventory(int quantity) {
        this.inventory += quantity;
    }
    
    // 재고 확인
    public boolean hasStock(int quantity) {
        return this.inventory >= quantity;
    }
    
    // 품절 여부
    public boolean isOutOfStock() {
        return this.inventory <= 0;
    }
    
    // 마감 임박 여부 (재고 10개 이하)
    public boolean isLowStock() {
        return this.inventory > 0 && this.inventory <= 10;
    }

}
