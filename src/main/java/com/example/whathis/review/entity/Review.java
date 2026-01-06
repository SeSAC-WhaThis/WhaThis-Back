package com.example.whathis.review.entity;

import com.example.whathis.BaseEntity;
import com.example.whathis.product.entity.Product;
import com.example.whathis.review.dto.request.ReviewCreateRequest;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 리뷰 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 평점 (1~5점)
    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer star;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 상품
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 리뷰 이미지 (여러 장 가능, 간단히 JSON 배열 문자열로 저장)
    @Column(columnDefinition = "TEXT")
    private String imageUrls; // ["url1", "url2", ...] 형태

    // 리뷰 도움됨 수
    @Column(nullable = false)
    private Integer helpfulCount = 0;

    @Builder
    private Review(
            String content, Integer star,
            User user, Product product,
            String imageUrls) {
        this.content = content;
        this.star = star;
        this.user = user;
        this.product = product;
        this.imageUrls = imageUrls;
        this.helpfulCount = 0;
    }

    public static Review of(
            ReviewCreateRequest request, User user,
            Product product) {
        return Review.builder()
                .content(request.getContent())
                .star(request.getStar())
                .imageUrls(request.getImageUrls())
                .user(user)
                .product(product)
                .build();
    }

    // 리뷰 수정 (선택적 업데이트)
    public void update(String content, Integer star, String imageUrls) {

        if (content != null && !content.isBlank()) {
            this.content = content;
        }

        if (star != null && star >= 1 && star <= 5) {
            this.star = star;
        }

        if (imageUrls != null) {
            this.imageUrls = imageUrls;
        }

    }

    // 리뷰 작성자 확인
    public boolean isOwnedBy(User user) {
        return this.user.getId().equals(user.getId());
    }

    // 도움됨 수 증가
    public void increaseHelpfulCount() {
        this.helpfulCount++;
    }

}
