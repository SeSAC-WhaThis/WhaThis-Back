package com.example.whathis.inventory.entity;

import com.example.whathis.BaseEntity;
import com.example.whathis.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "inventories")
@NoArgsConstructor
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상품과 1:1 관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    // 총 재고 수량
    @Column(nullable = false)
    private Integer totalStock;

    // 예약된 재고 (펀딩 참여로 예약된 수량)
    @Column(nullable = false)
    private Integer reservedStock = 0;

    // 남은 재고
    @Column(nullable = false)
    private Integer availableStock;

    // 재고 임계값 (마감 임박 알림용)
    private Integer lowStockThreshold = 10;

    // 재고 상태 체크
    public boolean isLowStock() {
        return availableStock != null && availableStock <= lowStockThreshold;
    }

    // 재고 소진
    public boolean isOutOfStock() {
        return availableStock != null && availableStock <= 0;
    }

}
