package com.example.whathis.order.repository;

import com.example.whathis.common.order.OrderStatus;
import com.example.whathis.order.entity.Order;
import com.example.whathis.product.entity.Product;
import com.example.whathis.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findAllByBuyerOrderByIdDesc(User buyer);

    List<Order> findAllByProduct(Product product);


    @Query("SELECT o FROM Order o JOIN FETCH o.product WHERE o.buyer = :buyer AND o.id = :orderId")
    Optional<Order> findByBuyerAndId(@Param("buyer") User buyer, @Param("orderId") Long orderId);

    // 구매자가 해당 유저인 모든 주문을 삭제
    void deleteAllByBuyer(User buyer);

    // 상품의 판매자가 해당 유저인 모든 주문을 삭제
    void deleteAllByProductSeller(User seller);

    // 특정 상품에 대한 유효한 주문 목록을 가진 구매자 목록 조회
    // PENDING 상태는 제외 - 결제를 완료한 상태가 아니므로 실구매자가 아님
    // CANCELLED 상태 구분
    // - 구매자가 직접 취소한 경우는 조회 대상에서 제외해야함
    // - 펀딩 실패로 인한 자동 환불인 경우는 실제 펀딩에 끝까지 참여한 구매자이므로 조회 대상에 포함
    // 즉, 성공/실패로 끝난 상품 모두 펀딩이 종료될 때까지 결제 완료였던 구매자만 조회하는 쿼리
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.product p " +
            "JOIN FETCH o.buyer b " +
            "WHERE p.id = :productId AND p.seller = :seller " +
            "AND ( " +
            "(o.status != :pendingStatus AND o.status != :cancelStatus )" +
            "OR (" +
            "o.status = :cancelStatus AND o.cancellationReason = :cancelReason) " +
            ") " +
            "ORDER BY o.createdAt DESC")
    List<Order> findAllByProductIdAndSeller(
            @Param("productId") Long productId,
            @Param("seller") User seller,
            @Param("pendingStatus") OrderStatus pendingStatus,
            @Param("cancelStatus") OrderStatus cancelStatus,
            @Param("cancelReason") String cancelReason);
}
