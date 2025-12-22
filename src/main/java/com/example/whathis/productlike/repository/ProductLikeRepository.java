package com.example.whathis.productlike.repository;

import com.example.whathis.productlike.entity.ProductLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    // 상품의 좋아요 수 조회
    Long countByProductId(Long productId);

    // 사용자가 특정 상품을 좋아요 했는지 확인
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // 사용자의 특정 상품 좋아요 조회
    Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId);

}
