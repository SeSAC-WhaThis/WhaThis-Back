package com.example.whathis.payment.repository;

import com.example.whathis.order.entity.Order;
import com.example.whathis.payment.entity.Payment;
import com.example.whathis.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);

    // 해당 유저가 결제한 내역 모두 삭제
    void deleteAllByUser(User user);

    // 해당 유저의 상품에 연관된 결제 내역 모두 삭제
    void deleteAllByOrderProductSeller(User user);
}
