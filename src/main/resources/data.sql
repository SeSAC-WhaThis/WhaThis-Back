-- 데이터베이스 선택
USE whathis;

-- 유저 샘플 데이터 --
INSERT INTO users (id, email, password, name, nickname, phone_number, address, profile_image_url, brn, created_at, updated_at)
VALUES (1, 'makersesac@sesac.com', '$2a$10$abcdEFGhijklMNOpqrstUVWXyz0123456789abcdeFGHJKLMNO', '첫째 새싹이', "1빠", '010-1234-5678',
    '서울시 성동구 자동차시장1길 64', 'https://example.com/profile1.png', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO users (id, email, password, name, nickname, phone_number, address, profile_image_url, brn, created_at, updated_at)
VALUES (2, 'supportersesac2@sesac.com', '$2a$10$abcdfasdfEFGhijklMNOpqrstUVWXyz0123456789abcdeFGHJKLMNO', '둘째 새싹이', "2빠", '010-9876-5432',
        '서울시 성동구 자동차시장1길 65', 'https://example.com/profile2.png', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- 카테고리 샘플 데이터 --
-- 1. 상위 카테고리 (1개)
INSERT INTO categories (id, name, description, parent_id, created_at, updated_at)
VALUES (1, '테크·가전', '최신 테크 & 전자기기', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
-- 2. 하위 카테고리 (2개)
INSERT INTO categories (id, name, description, parent_id, created_at, updated_at)
VALUES (2,'스마트기기', '스마트워치, 웨어러블 등', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, '홈가전', '주방·생활가전', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
