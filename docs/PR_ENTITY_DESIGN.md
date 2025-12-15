# 🎯 [Entity] 9개 엔티티 설계 및 구현

## 📋 작업 개요

**9개 핵심 엔티티** 설계 및 초기 구현을 완료했습니다.

### 관련 이슈
- #1

### 작업 기간
- 2024.12.14

---

## ✨ 주요 변경 사항

### 1️⃣ 엔티티 생성 (9개)

| 담당 | 엔티티 | 설명 |
|------|--------|------|
| 김규리 | `User` | 사용자 (판매자/구매자 구분) |
| 김규리 | `Order` | 주문/펀딩 참여 (예약 결제) |
| 김규리 | `Payment` | 결제 (PG 연동 준비) |
| 김규리 | `Follow` | 팔로우 관계 |
| 김규리 | `Review` | 리뷰 (평점, 이미지) |
| 김수환 | `Product` | 펀딩 상품 (목표금액, 현재금액, 참여자수) |
| 김수환 | `Inventory` | 재고 관리 (예약재고, 마감임박 체크) |
| 김수환 | `Category` | 카테고리 (계층 구조) |
| 김수환 | `ProductLike` | 상품 좋아요 |

### 2️⃣ Enum 분리 (6개)

```
src/main/java/com/example/whathis/common/
├── user/UserRole.java
├── product/ProductStatus.java
├── order/OrderStatus.java
└── payment/
    ├── PaymentStatus.java
    ├── PaymentMethod.java
    └── PaymentType.java
```

## 🎨 주요 설계 결정

**펀딩 플로우**:
```
PREPARING → ONGOING → SUCCESS/FAILED → CLOSED
```

---

### 1. 예약 결제 처리 (Order + Payment)

#### Order 상태 전환
```
PENDING → RESERVED → PAYMENT_PENDING → PAID
        ↓ (펀딩 실패)
      CANCELLED → REFUNDED
```

#### Payment 타입
```java
public enum PaymentType {
    RESERVED,   // 예약 결제 (펀딩 참여 시)
    IMMEDIATE   // 즉시 결제 (펀딩 성공 후)
}
```

---

### 2. 재고 관리 (Inventory)

```java
@Entity
public class Inventory {
    
    private Integer totalStock;      // 총 재고
    private Integer reservedStock;   // 예약된 재고
    private Integer availableStock;  // 남은 재고
    
    // 마감 임박 체크
    public boolean isLowStock() {
        return availableStock <= lowStockThreshold;
    }
}
```

---

### 3. orderNumber / paymentNumber 도입

**목적**: 외부 API 노출 시 보안을 위해 `id` 대신 사용

```bash
✅ GET /api/orders/ORD-20251214-A3B9C2F1
❌ GET /api/orders/123  (추측 가능)
```

---

### 4. 중복 방지 제약 조건

#### Follow (팔로우 중복 방지)
```java
@UniqueConstraint(columnNames = {"follower_id", "following_id"})
```

#### ProductLike (좋아요 중복 방지)
```java
@UniqueConstraint(columnNames = {"user_id", "product_id"})
```

---

## 📊 ERD 개요

```
User (판매자/구매자)
  ├─ 1:N → Product (펀딩 상품)
  │         ├─ 1:1 → Inventory (재고)
  │         ├─ N:1 → Category (카테고리)
  │         └─ 1:N → Order (펀딩 참여)
  │                   └─ 1:1 → Payment (결제)
  │
  ├─ M:N → ProductLike → Product (좋아요)
  ├─ M:N → Follow → User (팔로우)
  └─ 1:N → Review → Product (리뷰)
```

---

## 🔧 기술 스택

- Java 21
- Spring Boot 3.5.8
- Spring Data JPA
- Lombok
- MySQL

---

## 📚 참고 자료

- [엔티티 설계 상세 문서](ENTITY_DESIGN.md)
- [README.md](../README.md) - 프로젝트 개요

---

## 👥 작성자

- **김규리**: User, Order, Payment, Follow, Review
- **김수환**: Product, Inventory, Category, ProductLike

---

## 🚀 다음 단계

1. Repository 인터페이스 작성
2. DTO 설계 (Request/Response)
3. Service 로직 구현 (펀딩 참여, 종료 판별)
4. API Controller 구현

