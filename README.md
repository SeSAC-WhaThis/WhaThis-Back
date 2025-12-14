# WhaThis?!

SeSAC **1차 팀 프로젝트**로 Wadiz를 벤치마킹한 **e-Commerce / 크라우드 펀딩** 웹 서비스 **WhaThis**의 백엔드 레포지토리입니다.

짧은 개발 기간을 고려해 **펀딩(Funding) 기능을 우선 구현**하는 것을 1차 목표로 합니다.

---

## 프로젝트 정보

- **도메인**: e-Commerce

- **프로젝트명**: WhaThis

- **팀명**: 용수리


- **기간**
  - **기획 / 정리** : 2025.12.12 ~ 2025.12.16.
  - **개발** : 2025.12.17 ~ 2026.01.08.
  

- **팀 구성**: 3명
  - **Front-end**: 1인(이용민)
  - **Back-end**: 2인(김규리, 김수환)

---

## 핵심 범위 (MVP)

### Wadiz 대표 기능 중 이번 MVP 우선순위

- **펀딩(Funding)**: 출시 전/최초 공개 제품을 목표 금액 달성 시 프로젝트가 진행되는 구조
  - 사용자 역할: **판매자 / 구매자** (필터 또는 권한에 따라 기능/화면 분리)
  - 주요 도메인: 상품, 재고, 주문/결제(예약 결제), 오픈 예정(특정 시간에 공개)

### 펀딩 기능 플로우(업무 흐름)

1. **프로젝트 오픈**
2. **펀딩 참여(예약 결제)**
3. **마감 임박(재고 관리)**
4. **종료(성공/실패 판별)**

---

## 참고 기능 (확장 후보)

- **국가별 카테고리 정렬이 다름**
- **최근 검색어**
- **프리오더(Pre-order)**: 이미 시장에 출시된 제품을 Wadiz 사용자에게 혜택과 함께 제공
- **오픈 예정(Coming Soon)**: 판매자가 설정한 특정 시간에 펀딩이 열림

---

## 엔티티 구성

> 아래는 현재 합의된 “큰 덩어리” 기준이며, 상세 필드 / 관계는 구현 과정에서 구체화합니다.

- **User**
- **Product**
- **Inventory**
- **Payment**
- **Order**
- **Follow**
- **Category**
- **ProductLike**
- **Review**

### 모델링 추가 결정 사항

- **상품 좋아요**: `ProductLike` 엔티티로 분리
- **조회수**: `Product` 엔티티 내 `Integer` 필드로 관리
- **카테고리**: `Category` 엔티티로 분리

---

## 백엔드 역할 분담
### 김규리

- 유저(User)
- 주문(Order)
- 결제(Payment)
- 리뷰(Review)
- 팔로우(Follow)

### 김수환

- 상품(Product)
- 재고 관리(Inventory)
- 카테고리(Category)
- 상품 좋아요(ProductLike)

---

## 기술 스택

- Java 21
- Spring Boot 3.5.8
- Gradle
- Spring Data JPA
- Spring Security
- Bean Validation
- MySQL (예정: Redis)
- Lombok
- JUnit (spring-boot-starter-test), spring-security-test

---

### DB 설정

`application-local.properties`를 만들고 profile로 실행
  - 예: `src/main/resources/application-local.properties` 생성 후

```properties
# application-local.properties (예시)
spring.datasource.url=jdbc:mysql://localhost:3306/whathis?serverTimezone=Asia/Seoul&characterEncoding=utf8
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

## 협업 규칙(초안)

### 브랜치 전략

- `main`: 배포/제출 기준 브랜치
- `dev`: 통합 개발 브랜치
- `feat/엔티티명`: 기능 개발 브랜치 (예: `feat/user`)
- `fix/엔티티명`: 버그 수정 브랜치

### 커밋 메시지 컨벤션

- `feat/엔티티명`: 기능 추가 or 새로운 작업
- `fix/엔티티명`: 코드 수정 (버그 등)
- `refactor/엔티티명`: 리팩토링 작업
- `docs/엔티티명`: 문서 변경
- `chore/엔티티명`: 설정/빌드 작업 or 주석 수정

---

## API 설계 원칙 (초안)

1. **RESTful**한 리소스 중심 설계
2. **권한**(판매자/구매자) 및 접근 제어는 `Spring Security` 기반으로 구현
3. **에러 응답**은 공통 포맷으로 통일(예: `code`, `message`, `errors`)

---

## 앞으로 정리할 문서(추가 예정)

- [엔티티 설계 문서](ENTITY_DESIGN.md) ✅ **완료**
- 기능 명세서 (MVP 기준)
- API 명세서 (Endpoint / Request/Response / Error)
- DB 스키마 / ERD
