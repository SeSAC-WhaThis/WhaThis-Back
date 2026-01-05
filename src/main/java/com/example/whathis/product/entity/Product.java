package com.example.whathis.product.entity;

import com.example.whathis.BaseEntity;
import com.example.whathis.category.entity.Category;
import com.example.whathis.product.dto.request.ProductCreateRequest;
import com.example.whathis.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    // 조회 수
    @Column(nullable = false)
    private Integer viewCount = 0;

    // 대표 이미지
    @Column(nullable = false)
    private String thumbnailImageUrl;

    // 스토리 이미지 (상세 페이지에서 보여줄 이미지)
    @Column(nullable = false)
    private String storyImageUrl;

    @Builder
    private Product(
            String title, String description, User seller, Category category,
            BigDecimal price, BigDecimal goalAmount, LocalDateTime startDate,
            LocalDateTime endDate, String thumbnailImageUrl, String storyImageUrl, Integer inventory) {
        this.title = title;
        this.description = description;
        this.seller = seller;
        this.category = category;
        this.price = price;
        this.goalAmount = goalAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.storyImageUrl = storyImageUrl;
        this.inventory = inventory;

        this.currentAmount = BigDecimal.ZERO;
        this.buyerCount = 0;
        this.viewCount = 0;
    }

    // Product 생성 (서비스 레이어에서 업로드된 URL을 받아 생성)
    public static Product of(
            ProductCreateRequest request,
            User seller,
            Category category,
            String thumbnailImageUrl,
            String storyImageUrl) {
        return Product.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .seller(seller)
                .category(category)
                .price(request.getPrice())
                .goalAmount(request.getGoalAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .thumbnailImageUrl(thumbnailImageUrl)
                .storyImageUrl(storyImageUrl)
                .inventory(request.getInventory())
                .build();
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

    // 펀딩 성공 여부 확인
    public boolean isSuccess() {
        return currentAmount.compareTo(goalAmount) >= 0;
    }

    // 상품 정보 수정 (선택적 업데이트)
    public void update(
            String title, String description, Category category,
            LocalDateTime endDate, String thumbnailImageUrl, String storyImageUrl) {
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
                throw new IllegalArgumentException("종료일은 연장만 가능합니다. 현재 종료일: " + this.endDate);
            }
            this.endDate = endDate;
        }

        if (thumbnailImageUrl != null) {
            this.thumbnailImageUrl = thumbnailImageUrl;
        }

        if (storyImageUrl != null) {
            this.storyImageUrl = storyImageUrl;
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
        // 펀딩 모금액 증가
        this.currentAmount = this.currentAmount.add(
                this.price.multiply(BigDecimal.valueOf(quantity))
        );
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
