package com.example.whathis.order.repository;

import com.example.whathis.order.entity.Order;
import com.example.whathis.product.entity.Product;
import com.example.whathis.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findAllByBuyerOrderByIdDesc(User buyer);

    List<Order> findAllByProduct(Product product);

    Optional<Order> findByBuyerAndId(User buyer, Long orderId);
}
