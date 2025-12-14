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
import java.math.BigDecimal;
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
    private String thumbnailImageUrl;

}
