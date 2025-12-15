# 엔티티 설계 문서

## 전체 구조 (ERD 개요)

9개 엔티티로 Wadiz 펀딩 기능을 구현합니다.

```
User (판매자/구매자)
  ↓ (1:N)
Product (펀딩 상품)
  ↔ (1:1) Inventory (재고 관리)
  ↔ (N:1) Category (카테고리)
  ↓ (1:N)
Order (펀딩 참여/주문)
  ↔ (1:1) Payment (예약 결제)
  ↓ (1:N)
Review (리뷰)

User ↔ ProductLike ↔ Product (좋아요)
User ↔ Follow ↔ User (팔로우)
```

---

## 1. User (사용자)

**담당**: 김규리  
**테이블명**: `users`

### 핵심 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | PK |
| `email` | String | 이메일 (unique) |
| `password` | String | 비밀번호 (암호화) |
| `name` | String | 이름 |
| `role` | UserRole | **BUYER/SELLER/ADMIN** |
| `businessName` | String | 사업자명 (판매자) |
| `businessNumber` | String | 사업자번호 (판매자) |
| `status` | UserStatus | ACTIVE/INACTIVE/BANNED |
| `profileImageUrl` | String | 프로필 이미지 |

### 특징
- **역할 구분**: 판매자(프로젝트 오픈), 구매자(펀딩 참여)
- **판매자 정보**: 사업자명/번호 필드로 판매자 인증
- Spring Security 연동 예정

---

## 2. Category (카테고리)

**담당**: 김수환  
**테이블명**: `categories`

### 핵심 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | PK |
| `name` | String | 카테고리명 |
| `parent` | Category | 부모 카테고리 (self-join) |
| `children` | List<Category> | 자식 카테고리 |
| `displayOrder` | Integer | 정렬 순서 |
| `countryCode` | String | 국가별 정렬 (확장) |

### 특징
- **계층 구조**: 대분류 → 중분류 → 소분류
- **확장 후보**: 국가별 카테고리 정렬 다르게 설정

---

## 3. Product (상품/펀딩)

**담당**: 김수환  
**테이블명**: `products`

### 핵심 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | PK |
| `title` | String | 상품명 |
| `description` | TEXT | 상품 설명 |
| `seller` | User | 판매자 (FK) |
| `category` | Category | 카테고리 (FK) |
| `price` | BigDecimal | 가격 |
| `goalAmount` | BigDecimal | **펀딩 목표 금액** |
| `currentAmount` | BigDecimal | **현재 펀딩 금액** |
| `supporterCount` | Integer | 펀딩 참여자 수 |
| `startDate` | LocalDateTime | 펀딩 시작일 |
| `endDate` | LocalDateTime | 펀딩 종료일 |
| `scheduledOpenDate` | LocalDateTime | **오픈 예정 시간** |
| `status` | ProductStatus | PREPARING/ONGOING/SUCCESS/FAILED/CLOSED |
| `type` | ProductType | FUNDING/PRE_ORDER |
| `viewCount` | Integer | 조회수 |
| `thumbnailImageUrl` | String | 대표 이미지 |

### 특징
- **펀딩 핵심**: 목표금액/현재금액/참여자수
- **오픈 예정**: `scheduledOpenDate`에 자동 공개
- **펀딩 상태**:
  - `PREPARING`: 오픈 전
  - `ONGOING`: 진행중
  - `SUCCESS`: 목표 달성 종료
  - `FAILED`: 목표 미달 종료

---

## 4. Inventory (재고 관리)

**담당**: 김수환  
**테이블명**: `inventories`

### 핵심 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | PK |
| `product` | Product | 상품 (FK, unique) |
| `totalStock` | Integer | 총 재고 |
| `reservedStock` | Integer | 예약된 재고 |
| `availableStock` | Integer | 남은 재고 |
| `lowStockThreshold` | Integer | 마감 임박 기준 |

### 특징
- **마감 임박**: `availableStock ≤ lowStockThreshold`
- **예약 재고**: 펀딩 참여 시 `reservedStock` 증가
- 헬퍼 메서드: `isLowStock()`, `isOutOfStock()`

---

## 5. Order (주문/펀딩 참여)

**담당**: 김규리  
**테이블명**: `orders`

### 핵심 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | PK |
| `orderNumber` | String | 주문번호 (unique) |
| `buyer` | User | 구매자 (FK) |
| `product` | Product | 상품 (FK) |
| `quantity` | Integer | 수량 |
| `totalAmount` | BigDecimal | 주문 금액 |
| `status` | OrderStatus | 주문 상태 |
| `recipientName` | String | 수령인 |
| `shippingAddress` | String | 배송지 |
| `reservedPaymentDate` | LocalDateTime | **예약 결제 예정일** |
| `confirmedAt` | LocalDateTime | 확정 시간 |
| `cancelledAt` | LocalDateTime | 취소 시간 |

### 주문 상태 (OrderStatus)

```
PENDING          → 대기중
RESERVED         → 예약 완료 (펀딩 참여 완료)
PAYMENT_PENDING  → 결제 대기 (펀딩 성공 후)
PAID             → 결제 완료
PREPARING        → 배송 준비중
SHIPPED          → 배송중
DELIVERED        → 배송 완료
CANCELLED        → 취소 (펀딩 실패/사용자 취소)
REFUNDED         → 환불 완료
```

### 특징
- **예약 결제**: 펀딩 참여 시 `RESERVED` 상태, 종료 후 실제 결제
- 펀딩 실패 시 자동 `CANCELLED`

---

## 6. Payment (결제)

**담당**: 김규리  
**테이블명**: `payments`

### 핵심 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | PK |
| `paymentNumber` | String | 결제번호 (unique) |
| `order` | Order | 주문 (FK, unique) |
| `user` | User | 결제자 (FK) |
| `amount` | BigDecimal | 결제 금액 |
| `method` | PaymentMethod | 결제 방법 |
| `status` | PaymentStatus | PENDING/APPROVED/FAILED/CANCELLED/REFUNDED |
| `type` | PaymentType | **RESERVED/IMMEDIATE** |
| `pgProvider` | String | PG사 (토스/카카오페이) |
| `pgTransactionId` | String | PG 거래 ID |
| `paidAt` | LocalDateTime | 결제 완료 시간 |

### 결제 방법 (PaymentMethod)
- `CREDIT_CARD`, `KAKAO_PAY`, `TOSS_PAY`, `NAVER_PAY` 등

### 특징
- **예약 결제**: `type = RESERVED`, 펀딩 성공 시 `IMMEDIATE`로 전환
- PG사 연동 정보 저장

---

## 7. Follow (팔로우)

**담당**: 김규리  
**테이블명**: `follows`

### 핵심 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | PK |
| `follower` | User | 팔로우 하는 사람 (FK) |
| `following` | User | 팔로우 당하는 사람 (FK) |

### 특징
- **유니크 제약**: `(follower_id, following_id)` 중복 방지
- 판매자 팔로우 → 신상품 알림 기능 확장 가능

---

## 8. ProductLike (상품 좋아요)

**담당**: 김수환  
**테이블명**: `product_likes`

### 핵심 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | PK |
| `user` | User | 사용자 (FK) |
| `product` | Product | 상품 (FK) |

### 특징
- **유니크 제약**: `(user_id, product_id)` 중복 방지
- 좋아요 수는 집계 쿼리로 조회

---

## 9. Review (리뷰)

**담당**: 김규리  
**테이블명**: `reviews`

### 핵심 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | PK |
| `content` | TEXT | 리뷰 내용 |
| `rating` | Integer | 평점 (1~5) |
| `user` | User | 작성자 (FK) |
| `product` | Product | 상품 (FK) |
| `order` | Order | 주문 (FK) |
| `imageUrls` | TEXT | 리뷰 이미지 (JSON 배열) |
| `helpfulCount` | Integer | 도움됨 수 |

### 특징
- **구매 확정 후 작성**: `order` 연관
- **이미지 다중 업로드**: JSON 배열 문자열 저장
- 평균 평점 집계로 상품 평가

---

## 펀딩 플로우와 엔티티 관계

### 1. 프로젝트 오픈
- 판매자(`User.role=SELLER`)가 `Product` 생성
- `Product.status = PREPARING` → `scheduledOpenDate`에 `ONGOING`으로 변경
- `Inventory` 생성 (재고 설정)

### 2. 펀딩 참여 (예약 결제)
- 구매자(`User.role=BUYER`)가 `Order` 생성
- `Order.status = RESERVED`, `Payment.type = RESERVED`
- `Inventory.reservedStock` 증가
- `Product.currentAmount`, `supporterCount` 증가

### 3. 마감 임박 (재고 관리)
- `Inventory.availableStock ≤ lowStockThreshold`
- 프론트엔드에 "마감 임박" 뱃지 표시

### 4. 종료 (성공/실패 판별)
- 펀딩 종료 시각(`Product.endDate`) 도달
- **성공**: `currentAmount ≥ goalAmount`
  - `Product.status = SUCCESS`
  - `Order.status = PAYMENT_PENDING` → 실제 결제 진행
  - `Payment.status = APPROVED`, `type = IMMEDIATE`
- **실패**: `currentAmount < goalAmount`
  - `Product.status = FAILED`
  - `Order.status = CANCELLED`
  - `Payment.status = CANCELLED`
