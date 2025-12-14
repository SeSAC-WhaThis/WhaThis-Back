package com.example.whathis.review.entity;

import com.example.whathis.BaseEntity;
import com.example.whathis.order.entity.Order;
import com.example.whathis.product.entity.Product;
import com.example.whathis.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "reviews")
@NoArgsConstructor
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 리뷰 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 평점 (1~5점)
    @Column(nullable = false)
    @Min(1) @Max(5)
    private Integer star;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 상품
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 주문 정보 (구매 확정 후 작성 가능)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    // 리뷰 이미지 (여러 장 가능, 간단히 JSON 배열 문자열로 저장)
    @Column(columnDefinition = "TEXT")
    private String imageUrls; // ["url1", "url2", ...] 형태

    // 리뷰 도움됨 수
     @Column(nullable = false)
     private Integer helpfulCount = 0;

}
