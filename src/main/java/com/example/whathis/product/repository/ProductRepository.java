package com.example.whathis.product.repository;

import com.example.whathis.product.entity.Product;
import com.example.whathis.user.entity.User;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

   // N+1 방지: seller, category를 fetch join으로 한 번에 조회
   @Query("SELECT p FROM Product p " +
          "JOIN FETCH p.seller " +
          "LEFT JOIN FETCH p.category " +
          "ORDER BY p.category.name ASC")
   List<Product> findAllWithSellerAndCategory();
    
   // 페이징 & N+1 방지
   // 프론트엔드와의 연동을 위해서 오버로딩
    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.seller " +
           "LEFT JOIN FETCH p.category")
    Page<Product> findAllWithSellerAndCategory(Pageable pageable);

    // 단일 조회 & N+1 방지
    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.seller " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.id = :productId")
    Optional<Product> findByIdWithSellerAndCategory(@Param("productId") Long productId);

    // 판매자별 상품 조회 & N+1 방지
    // 내가 등록한 상품 목록을 조회
    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.seller " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.seller.id = :sellerId")
    List<Product> findAllMyProducts(@Param("sellerId") Long sellerId);

    // 특정 사용자의 총 누적 판매 금액 조회
    // 성공으로 종료된 상품의 누적 판매 금액이므로 endDate(판매 종료일)가 현재보다 과거여야하고,
    // 현재 모금액이 목표 모금액 이상이어야 함
    @Query("SELECT SUM(p.currentAmount) " +
            "FROM Product p " +
            "WHERE p.seller.id = :sellerId " +
            "AND p.endDate < CURRENT_TIMESTAMP " +
            "AND p.currentAmount >= p.goalAmount")
    BigDecimal sumSalesTotalBySellerId(@Param("sellerId") Long sellerId);

    // 특정 사용자의 진행 중인 상품 목록 조회
    // startDate(시작일)는 지났고 endDate(종료일)은 지나지 않은 상품
    @Query("SELECT p " +
            "FROM Product p " +
            "JOIN FETCH p.seller " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.seller.id = :sellerId " +
            "AND p.startDate <= CURRENT_TIMESTAMP " +
            "AND p.endDate > CURRENT_TIMESTAMP " +
            "ORDER BY p.startDate DESC")
    List<Product> findProductsBySellerAndStatus(@Param("sellerId") Long sellerId);
  
    // 카테고리별 상품 조회 & N+1 방지
    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.seller " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.category.id = :categoryId")
    List<Product> findAllByCategoryId(@Param("categoryId") Long categoryId);

}
