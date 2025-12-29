package com.example.whathis.product.repository;

import com.example.whathis.product.entity.Product;
import com.example.whathis.user.entity.User;
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
           "LEFT JOIN FETCH p.category")
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

}
