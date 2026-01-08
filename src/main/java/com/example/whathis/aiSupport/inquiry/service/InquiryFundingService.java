package com.example.whathis.aiSupport.inquiry.service;

import com.example.whathis.category.entity.Category;
import com.example.whathis.category.repository.CategoryRepository;
import com.example.whathis.common.exception.BusinessException;
import com.example.whathis.common.exception.ErrorCode;
import com.example.whathis.common.order.OrderStatus;
import com.example.whathis.order.dto.response.OrderCreateResponse;
import com.example.whathis.order.entity.Order;
import com.example.whathis.order.repository.OrderRepository;
import com.example.whathis.product.dto.response.ProductDetailResponse;
import com.example.whathis.product.entity.Product;
import com.example.whathis.product.repository.ProductRepository;
import com.example.whathis.user.entity.User;
import com.example.whathis.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InquiryFundingService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    // ToolContext 등에서 전달된 userId로 사용자를 조회(없거나 유효하지 않으면 null)
    @Transactional(readOnly = true)
    public User findUserByIdOrNull(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }

    // AI 상담에서 수집한 입력값으로 펀딩 상품(프로젝트)을 생성
    @Transactional
    public ProductDetailResponse createFundingProduct(
        User seller,
        String brn,
        String title,
        String description,
        BigDecimal price,
        BigDecimal goalAmount,
        Long categoryId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String thumbnailImageUrl,
        String storyImageUrl,
        Integer inventory) {
        requireLoggedIn(seller);
        ensureSellerBrn(seller, brn);
        validateFundingProductInput(title, description, price, goalAmount, categoryId, startDate, endDate, inventory);

        String thumbnailUrl = requireTrimmed(thumbnailImageUrl, "썸네일 이미지 URL은 필수입니다.");
        String storyUrl = requireTrimmed(storyImageUrl, "스토리 이미지 URL은 필수입니다.");
        Category category = findCategoryOrThrow(categoryId);
        validateFundingDates(startDate, endDate);

        Product product = Product.builder()
            .title(title.trim())
            .description(description.trim())
            .seller(seller)
            .category(category)
            .price(price)
            .goalAmount(goalAmount)
            .startDate(startDate)
            .endDate(endDate)
            .thumbnailImageUrl(thumbnailUrl)
            .storyImageUrl(storyUrl)
            .inventory(inventory)
            .build();

        Product savedProduct = productRepository.save(product);
        return ProductDetailResponse.from(savedProduct, 0L, false);
    }

    // AI 상담에서 수집한 입력값으로 펀딩 참여 주문을 생성
    @Transactional
    public OrderCreateResponse createFundingOrder(User buyer, Long productId, Integer quantity, String requestNote) {
        requireLoggedIn(buyer);
        validateFundingOrderInput(buyer, productId, quantity);

        Product product = findProductOrThrow(productId);

        if (!product.hasStock(quantity)) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }

        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(quantity));

        Order order = Order.builder()
            .buyer(buyer)
            .product(product)
            .quantity(quantity)
            .totalAmount(totalAmount)
            .receiverName(buyer.getName())
            .receiverPhone(buyer.getPhoneNumber())
            .receiverAddress(buyer.getAddress())
            .request(requestNote)
            .status(OrderStatus.PENDING)
            .build();

        orderRepository.save(order);
        return OrderCreateResponse.from(order);
    }

    // 비로그인 사용자를 막는 로직
    private void requireLoggedIn(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }

    // 판매자가 사업자등록번호를 아직 등록하지 않았다면 brn을 저장
    private void ensureSellerBrn(User seller, String brn) {
        if (seller.getBrn() != null) {
            return;
        }
        if (brn == null || brn.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사업자등록번호는 필수입니다.");
        }
        seller.setBrn(brn.trim());
        userRepository.save(seller);
    }

    // 펀딩 프로젝트(상품) 오픈 입력값을 검증
    private void validateFundingProductInput(
        String title,
        String description,
        BigDecimal price,
        BigDecimal goalAmount,
        Long categoryId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer inventory) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "상품 제목은 필수입니다.");
        }
        if (description == null || description.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "상품 설명은 필수입니다.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "가격은 0보다 커야 합니다.");
        }
        if (goalAmount == null || goalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "목표 금액은 0보다 커야 합니다.");
        }
        if (categoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "카테고리는 필수입니다.");
        }
        if (startDate == null || endDate == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "펀딩 시작일/종료일은 필수입니다.");
        }
        if (inventory == null || inventory <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "재고 수량은 1개 이상이어야 합니다.");
        }
    }

    // 펀딩 참여(주문 생성) 입력값을 검증
    private void validateFundingOrderInput(User buyer, Long productId, Integer quantity) {
        if (productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "상품 ID는 필수입니다.");
        }
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "수량은 1개 이상이어야 합니다.");
        }

        if (buyer.getPhoneNumber() == null || buyer.getPhoneNumber().isBlank()
            || buyer.getAddress() == null || buyer.getAddress().isBlank()) {
            throw new BusinessException(
                ErrorCode.INVALID_INPUT,
                "주문 생성을 위해 연락처/주소가 필요합니다. 마이페이지에서 배송지 정보(연락처, 주소)를 먼저 등록해 주세요.");
        }
    }

    // 이미지 URL처럼 공백이 들어오기 쉬운 값을 trim 해서 반환
    private String requireTrimmed(String value, String messageWhenBlank) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, messageWhenBlank);
        }
        return value.trim();
    }

    // categoryId로 카테고리를 조회
    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."));
    }

    // productId로 상품을 조회
    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    // 펀딩 시작/종료일이 서비스 정책에 맞는지 검증
    private void validateFundingDates(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime now = LocalDateTime.now();

        if (startDate.isBefore(now.minusDays(1))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "시작일은 과거일 수 없습니다");
        }

        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "종료일은 시작일보다 이전일 수 없습니다");
        }

        if (endDate.isBefore(startDate.plusDays(7))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "펀딩 기간은 최소 7일 이상이어야 합니다");
        }

        if (endDate.isAfter(startDate.plusDays(90))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "펀딩 기간은 최대 90일을 초과할 수 없습니다");
        }
    }
}
