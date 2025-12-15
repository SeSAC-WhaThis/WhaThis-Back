# ApiResponse 사용 가이드

## 📦 생성된 파일

```
src/main/java/com/example/whathis/common/
├── response/
│   ├── ApiResponse.java       # 공통 응답 래퍼
│   └── ErrorResponse.java     # 에러 응답
└── exception/
    ├── ErrorCode.java          # 에러 코드 정의
    ├── BusinessException.java  # 비즈니스 예외
    └── GlobalExceptionHandler.java  # 전역 예외 처리
```

---

## 📘 사용법

### 1️⃣ Controller에서 성공 응답

```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    // ✅ 데이터만 반환
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
        @PathVariable Long productId
    ) {
        ProductResponse product = productService.getProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
    
    // ✅ 데이터 + 메시지
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
        @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(product, "상품이 등록되었습니다"));
    }
    
    // ✅ 메시지만 (데이터 없음)
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
        @PathVariable Long productId
    ) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(
            ApiResponse.successWithMessage("상품이 삭제되었습니다")
        );
    }
    
    // ✅ 페이지네이션
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<ProductResponse> products = productService.getProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
}
```

---

### 2️⃣ Service에서 예외 발생

```java
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public ProductResponse getProduct(Long productId) {
        // ❌ 상품 없으면 예외 발생
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        
        return ProductResponse.from(product);
    }
    
    public void createOrder(OrderCreateRequest request) {
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        
        // 펀딩 상태 체크
        if (product.getStatus() != ProductStatus.ONGOING) {
            throw new BusinessException(ErrorCode.FUNDING_NOT_ONGOING);
        }
        
        // 재고 체크
        Inventory inventory = product.getInventory();
        if (inventory.getAvailableStock() < request.getQuantity()) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
        
        // 주문 생성...
    }
}
```

---

### 3️⃣ 실제 응답 예시

#### ✅ 성공 응답

```http
GET /api/products/1
```

**Response (200 OK)**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "혁신적인 스마트워치",
    "price": 150000,
    "goalAmount": 10000000,
    "currentAmount": 7500000,
    "status": "ONGOING"
  }
}
```

#### ✅ 생성 성공 + 메시지

```http
POST /api/products
```

**Response (201 Created)**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "혁신적인 스마트워치"
  },
  "message": "상품이 등록되었습니다"
}
```

#### ❌ 에러 응답 (리소스 없음)

```http
GET /api/products/999
```

**Response (404 Not Found)**
```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "상품을 찾을 수 없습니다"
  }
}
```

#### ❌ Validation 에러

```http
POST /api/products
{
  "title": "",
  "price": -1000
}
```

**Response (400 Bad Request)**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_INPUT",
    "message": "잘못된 입력 값입니다",
    "details": [
      {
        "field": "title",
        "value": "",
        "reason": "제목은 필수입니다"
      },
      {
        "field": "price",
        "value": "-1000",
        "reason": "가격은 0 이상이어야 합니다"
      }
    ]
  }
}
```

#### ❌ 비즈니스 로직 에러

```http
POST /api/orders
{
  "productId": 1,
  "quantity": 100
}
```

**Response (409 Conflict)**
```json
{
  "success": false,
  "error": {
    "code": "OUT_OF_STOCK",
    "message": "재고가 부족합니다"
  }
}
```

---

## 🎯 ErrorCode 사용법

### 새로운 에러 코드 추가

```java
// ErrorCode.java에 추가
public enum ErrorCode {
    // ...
    
    // 새로운 에러
    SELLER_ONLY("SELLER_ONLY", "판매자만 접근할 수 있습니다"),
    BUYER_ONLY("BUYER_ONLY", "구매자만 접근할 수 있습니다");
}
```

### 커스텀 메시지로 예외 발생

```java
// 기본 메시지 사용
throw new BusinessException(ErrorCode.OUT_OF_STOCK);

// 커스텀 메시지 사용
throw new BusinessException(
    ErrorCode.OUT_OF_STOCK, 
    "현재 남은 재고는 " + availableStock + "개입니다"
);
```

---

## 🔧 GlobalExceptionHandler

자동으로 모든 예외를 잡아서 ApiResponse 형식으로 반환합니다.

### 처리되는 예외들

1. **BusinessException**: 비즈니스 로직 예외 → 정의된 ErrorCode 반환
2. **MethodArgumentNotValidException**: @Valid 실패 → 필드별 상세 에러
3. **BindException**: 바인딩 실패
4. **IllegalArgumentException**: 잘못된 인자
5. **Exception**: 기타 모든 예외 → 500 Internal Server Error

---

## 💡 추가 기능

### 1. 커스텀 예외 추가

```java
// 특정 도메인 예외
public class ProductNotFoundException extends BusinessException {
    public ProductNotFoundException() {
        super(ErrorCode.PRODUCT_NOT_FOUND);
    }
    
    public ProductNotFoundException(Long productId) {
        super(ErrorCode.PRODUCT_NOT_FOUND, 
              "상품을 찾을 수 없습니다 (ID: " + productId + ")");
    }
}
```

### 2. @RestControllerAdvice 범위 지정

```java
// 특정 패키지만 처리
@RestControllerAdvice(basePackages = "com.example.whathis.product")
public class ProductExceptionHandler {
    // Product 관련 예외만 처리
}
```

---

## 🎨 Response 구조 정리

```java
// 성공
ApiResponse<T>
├─ success: true
├─ data: T
└─ message: String (optional)

// 에러
ApiResponse<Void>
├─ success: false
└─ error: ErrorResponse
    ├─ code: String
    ├─ message: String
    └─ details: List<FieldError> (optional)
        └─ field, value, reason
```

---

## 📋 체크리스트

- [x] ApiResponse 제네릭 래퍼 클래스
- [x] ErrorResponse 구조
- [x] ErrorCode enum 정의
- [x] BusinessException 커스텀 예외
- [x] GlobalExceptionHandler 전역 처리
- [x] Validation 에러 상세 정보
- [x] HTTP Status 자동 매핑


