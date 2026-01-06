package com.example.whathis.review.repository;

import com.example.whathis.review.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 사용자가 해당 상품에 이미 리뷰를 작성했는지 확인
    boolean existsByProductIdAndUserId(Long productId, Long userId);

    // 특정 상품의 모든 리뷰 조회
    // N+1 방지를 위한 fetch join
    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.user " +
            "WHERE r.product.id = :productId " +
            "ORDER BY r.createdAt DESC")
    List<Review> findAllByProductIdWithUserAndOrder(@Param("productId") Long productId);

    // 특정 사용자의 판매 상품에 대한 리뷰 평균 별점 조회
    @Query("SELECT AVG(r.star) " +
            "FROM Review r " +
            "WHERE r.product.seller.id = :sellerId")
    Double findRatingAvgBySellerId(@Param("sellerId") Long sellerId);
}
