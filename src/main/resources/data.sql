-- 데이터베이스 선택
USE whathis;

-- 유저 샘플 데이터 --
INSERT INTO users (id, email, password, name, nickname, phone_number, address, profile_image_url, brn, provider, created_at, updated_at)
VALUES (1, 'makersesac@sesac.com', '$2a$10$abcdEFGhijklMNOpqrstUVWXyz0123456789abcdeFGHJKLMNO', '첫째 새싹이', '1빠', '010-1234-5678',
    '서울시 성동구 자동차시장1길 64', 'https://example.com/profile1.png', NULL, 'LOCAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO users (id, email, password, name, nickname, phone_number, address, profile_image_url, brn, provider, created_at, updated_at)
VALUES (2, 'supportersesac2@sesac.com', '$2a$10$abcdfasdfEFGhijklMNOpqrstUVWXyz0123456789abcdeFGHJKLMNO', '둘째 새싹이', '2빠', '010-9876-5432',
        '서울시 성동구 자동차시장1길 65', 'https://example.com/profile2.png', NULL, 'LOCAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO users (id, email, password, name, nickname, phone_number, address, profile_image_url, brn, provider, created_at, updated_at)
VALUES (3, 'tester@sesac.com', '$2a$10$TestUserPasswordHashForTestingPurposesOnly1234567890', '셋째 새싹이', '테스터', '010-1111-2222',
        '서울시 강남구 테헤란로 123', 'https://example.com/profile3.png', NULL, 'LOCAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- 카테고리 샘플 데이터 (다양한 도메인 추가) --
INSERT INTO categories (id, name, created_at, updated_at) VALUES
(1, '가전·디지털', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '패션·잡화', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '뷰티·코스메틱', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '푸드', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '홈·리빙', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, '스포츠·아웃도어', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, '반려동물', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, '게임·취미', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, '디자인·문구', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, '여행·레저', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, '출판', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, '기부·후원', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- 상품 샘플 데이터 (카테고리 ID 매핑 수정) --
-- Product 1: 스마트워치 -> 테크·가전 (ID: 1)
INSERT INTO products (id, title, description, seller_id, category_id, price, inventory, goal_amount, current_amount, buyer_count, start_date, end_date, view_count, thumbnail_image_url, story_image_url, created_at, updated_at)
VALUES (1, '혁신적인 스마트워치 Pro', '건강 모니터링과 스마트 기능이 결합된 차세대 스마트워치입니다. 심박수, 혈압, 수면 패턴을 실시간으로 모니터링하고 50m 방수 기능을 지원합니다.',
        1, 1, 150000, 100, 10000000, 7500000, 50,
        DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY), DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 20 DAY),
        1251, 'https://example.com/smartwatch-pro.jpg', 'https://example.com/smartwatch-pro-story.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Product 2: 블렌더 -> 홈·리빙 (ID: 5)
INSERT INTO products (id, title, description, seller_id, category_id, price, inventory, goal_amount, current_amount, buyer_count, start_date, end_date, view_count, thumbnail_image_url, story_image_url, created_at, updated_at)
VALUES (2, '프리미엄 무선 블렌더', '강력한 모터와 스테인레스 날로 어떤 재료도 부드럽게 갈아냅니다. USB 충전식으로 어디서나 사용 가능하며, 500ml 대용량 용기를 제공합니다.',
        1, 5, 89000, 200, 5000000, 4200000, 48,
        DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY), DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 15 DAY),
        856, 'https://example.com/blender.jpg', 'https://example.com/blender-story.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- 주문 샘플 데이터 (Review 작성용) --
INSERT INTO orders (id, order_number, buyer_id, product_id, quantity, total_amount, status, reserved_payment_date, confirmed_at, cancelled_at, cancellation_reason, created_at, updated_at)
VALUES (1, 'ORD-20251210-A1B2C3D4', 1, 1, 2, 300000, 'CONFIRMED',
        DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 20 DAY),
        DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY),
        NULL, NULL, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 8 DAY), CURRENT_TIMESTAMP);

INSERT INTO orders (id, order_number, buyer_id, product_id, quantity, total_amount, status, reserved_payment_date, confirmed_at, cancelled_at, cancellation_reason, created_at, updated_at)
VALUES (2, 'ORD-20251212-E5F6G7H8', 2, 2, 1, 89000, 'CONFIRMED',
        DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 15 DAY),
        DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY),
        NULL, NULL, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY), CURRENT_TIMESTAMP);

INSERT INTO orders (id, order_number, buyer_id, product_id, quantity, total_amount, status, reserved_payment_date, confirmed_at, cancelled_at, cancellation_reason, created_at, updated_at)
VALUES (3, 'ORD-20251215-I9J0K1L2', 2, 1, 1, 150000, 'CONFIRMED',
        DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 20 DAY),
        CURRENT_TIMESTAMP,
        NULL, NULL, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), CURRENT_TIMESTAMP);
