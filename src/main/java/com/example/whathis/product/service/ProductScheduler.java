package com.example.whathis.product.service;

import com.example.whathis.common.order.OrderStatus;
import com.example.whathis.order.entity.Order;
import com.example.whathis.order.repository.OrderRepository;
import com.example.whathis.payment.service.PaymentService;
import com.example.whathis.product.entity.Product;
import com.example.whathis.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductScheduler {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    //@Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    @Scheduled(fixedDelay = 10000) // 테스트용으로 10초마다 실행
    public void checkFundingStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 환불 처리 대상 조회: 마감일이 지났는데 아직 RESERVED주문(결제완료상태)이 남아있는 상품들
        List<Product> refundTargetProducts = productRepository.findProductsWithReservedOrders(now, OrderStatus.RESERVED);

        refundTargetProducts.stream().forEach(product -> {
            // 해당 상품의 주문내역 불러오기
            List<Order> orders = orderRepository.findAllByProduct(product);

            // 펀딩 성공 여부 판단
            boolean isSuccess = product.getCurrentAmount().compareTo(product.getGoalAmount()) >= 0;

            if(isSuccess){
                handleSuccess(orders);
            } else {
                handleFailure(orders);
            }
        });
    }

    // 펀딩 성공한 경우
    private void handleSuccess(List<Order> orders) {
        orders.stream().filter(order -> order.getStatus() == (OrderStatus.RESERVED))
                .forEach(order -> {
                    order.setStatus(OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                });
    }

    // 펀딩 실패한 경우
    private void handleFailure(List<Order> orders) {
        orders.stream().filter(order -> order.getStatus() == (OrderStatus.RESERVED))
                .forEach(order -> {
                   try{
                       paymentService.refundBySystem(order);
                   } catch (Exception e) {
                       System.err.println("환불 실패한 Order ID: " + order.getId());
                       System.err.println("에러 발생 이유: " + e.getMessage());
                   }
                });
    }
}
