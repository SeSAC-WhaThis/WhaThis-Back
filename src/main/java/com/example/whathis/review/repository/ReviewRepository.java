package com.example.whathis.review.repository;

import com.example.whathis.review.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 해당 주문에 이미 리뷰가 작성되었는지 확인
    // 특정 ID의 주문(orderId)에 대한 리뷰(r)가 하나(COUNT(r) > 0)라도 있으면 true 반환
    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Review r
        WHERE r.order.id = :orderId
        """)
    boolean existsByOrderId(@Param("orderId") Long orderId);

    // 특정 상품의 모든 리뷰 조회
    // N+1 방지를 위한 fetch join
    @Query("SELECT r FROM Review r " +
           "JOIN FETCH r.user " +
           "LEFT JOIN FETCH r.order " +
           "WHERE r.product.id = :productId " +
           "ORDER BY r.createdAt DESC")
    List<Review> findAllByProductIdWithUserAndOrder(@Param("productId") Long productId);

    // 특정 사용자의 판매 상품에 대한 리뷰 평균 별점 조회
    @Query("SELECT AVG(r.star) " +
            "FROM Review r " +
            "WHERE r.product.seller.id = :sellerId")
    Double findRatingAvgBySellerId(@Param("sellerId") Long sellerId);
}
