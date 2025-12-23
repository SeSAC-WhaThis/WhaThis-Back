package com.example.whathis.config;

import com.example.whathis.common.order.OrderStatus;
import com.example.whathis.order.entity.Order;
import com.example.whathis.order.repository.OrderRepository;
import com.example.whathis.product.entity.Product;
import com.example.whathis.product.repository.ProductRepository;
import com.example.whathis.user.entity.User;
import com.example.whathis.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 이미 데이터가 존재하면 실행하지 않음 (이메일로 체크)
        if (userRepository.findByEmail("test@test.com").isPresent()) {
            return;
        }

        // 테스트 유저 생성
        String encodedPassword = passwordEncoder.encode("test1234!");

        User testUser = userRepository.save(
                User.builder()
                        .email("test@test.com")
                        .password(encodedPassword)
                        .name("테스트유저")
                        .nickname("테스터123")
                        .build());

        // 배송지 정보 설정
        testUser.setDeliveryInfo("010-0000-0000", "서울시 강남구");
        userRepository.save(testUser);

        // 상품 조회 (data.sql에서 생성된 ID 1번 상품)
        // 만약 없으면 건너뜀 (data.sql이 먼저 실행된다고 가정)
        Product product = productRepository.findById(1L).orElse(null);

        if (product != null) {
            // 주문 생성 (리뷰 작성을 위해 구매 확정 상태로 생성)
            Order order = Order.builder()
                    .buyer(testUser)
                    .product(product)
                    .quantity(1)
                    .totalAmount(product.getPrice())
                    .status(OrderStatus.CONFIRMED)
                    .reservedPaymentDate(LocalDateTime.now())
                    .confirmedAt(LocalDateTime.now())
                    .build();

            orderRepository.save(order);
            System.out.println("Test Order created: ID = " + order.getId());
        }
        System.out.println("Test User created: email = test@test.com, password = test1234");
    }
}
