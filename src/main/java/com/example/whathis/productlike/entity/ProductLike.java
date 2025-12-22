package com.example.whathis.productlike.entity;

import com.example.whathis.BaseEntity;
import com.example.whathis.product.entity.Product;
import com.example.whathis.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    name = "product_likes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"})
)
@NoArgsConstructor
public class ProductLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 좋아요 누른 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 좋아요 대상 상품
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder
    public ProductLike(User user, Product product) {
        this.user = user;
        this.product = product;
    }

}
