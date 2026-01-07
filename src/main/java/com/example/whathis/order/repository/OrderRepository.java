package com.example.whathis.order.repository;

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
}
