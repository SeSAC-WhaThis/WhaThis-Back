package com.example.whathis.productlike.repository;

import com.example.whathis.product.entity.Product;
import com.example.whathis.productlike.entity.ProductLike;
import java.util.List;
import java.util.Optional;

import com.example.whathis.user.entity.User;
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

    // 사용자가 좋아요를 누른 모든 상품 조회
    List<ProductLike> findAllByUserId(Long userId);

    // 해당 유저가 누른 좋아요 모두 삭제
    void deleteAllByUser(User user);

    // 해당 유저의 상품에 눌린 좋아요 모두 삭제
    void deleteAllByProductSeller(User user);

    void deleteAllByProduct(Product product);
}
