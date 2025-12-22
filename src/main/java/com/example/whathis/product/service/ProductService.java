package com.example.whathis.product.service;

import com.example.whathis.category.entity.Category;
import com.example.whathis.category.repository.CategoryRepository;
import com.example.whathis.common.exception.BusinessException;
import com.example.whathis.common.exception.ErrorCode;
import com.example.whathis.product.dto.request.ProductCreateRequest;
import com.example.whathis.product.dto.request.ProductUpdateRequest;
import com.example.whathis.product.dto.response.ProductDetailResponse;
import com.example.whathis.product.dto.response.ProductResponse;
import com.example.whathis.product.entity.Product;
import com.example.whathis.product.repository.ProductRepository;
import com.example.whathis.productlike.repository.ProductLikeRepository;
import com.example.whathis.user.entity.User;
import com.example.whathis.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductLikeRepository productLikeRepository;
    private final UserRepository userRepository; // 테스트용 임시 추가

    @Transactional
    public ProductResponse save(
        ProductCreateRequest request,
        User currentUser
    ) {
        // 1. 로그인 체크 (테스트용: 로그인 시스템 없을 시 샘플 유저 사용)
        if (currentUser == null) {
            // TODO: 실제 인증 시스템 구현 후 이 부분 제거하고 예외 던지기
            currentUser = userRepository.findById(1L)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, 
                    "테스트용 샘플 유저(id=1)가 DB에 없습니다. data.sql을 먼저 실행하세요."));
            System.out.println("[테스트] 샘플 유저(id=1) 사용 중");
        }

        // 실제 로그인 체크는 아래 if문 사용 (인증 시스템 구현 후)
        // if (currentUser == null) {
        //     throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        // }
        
        // 2. 사업자등록번호 체크 & 저장 (최초 상품 등록 시에만)
        if (currentUser.getBrn() == null) {
            // 최초 상품 등록 시 사업자등록번호가 필수
            if (request.getBrn() == null || request.getBrn().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "사업자등록번호는 필수입니다.");
            }
            // 사업자등록번호를 User에 저장
            currentUser.setBrn(request.getBrn());
            userRepository.save(currentUser); // User 업데이트
            System.out.println("사업자등록번호 등록: " + request.getBrn());
        }
        // 이미 등록된 사용자는 사업자등록번호 입력 생략 가능 (기존 번호 사용)
        
        // 3. 카테고리 조회
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."));
        
        // 4. 날짜 검증
        validateProductDates(request.getStartDate(), request.getEndDate());
        
        // 5. Product 생성
        Product product = Product.of(
                request.getTitle(),
                request.getDescription(),
                currentUser,  // 판매자
                category,
                request.getPrice(),
                request.getGoalAmount(),
                request.getStartDate(),
                request.getEndDate(),
                request.getThumbnailImageUrl(),
                request.getInventory()
        );
        
        // 6. Product 저장
        Product savedProduct = productRepository.save(product);
        
        // 7. ProductResponse 반환
        return ProductResponse.from(savedProduct);
    }
    
    // 상품 날짜 검증
    private void validateProductDates(
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        LocalDateTime now = LocalDateTime.now();
        
        // 시작일이 과거인지 확인 (당일은 허용)
        if (startDate.isBefore(now.minusDays(1))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "시작일은 과거일 수 없습니다");
        }
        
        // 종료일이 시작일보다 이전인지 확인
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "종료일은 시작일보다 이전일 수 없습니다");
        }
        
        // 펀딩 기간이 너무 짧은지 확인 (최소 7일)
        if (endDate.isBefore(startDate.plusDays(7))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "펀딩 기간은 최소 7일 이상이어야 합니다");
        }
        
        // 펀딩 기간이 너무 긴지 확인 (최대 90일)
        if (endDate.isAfter(startDate.plusDays(90))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "펀딩 기간은 최대 90일을 초과할 수 없습니다");
        }
    }

    // 제품 전체 조회
    public List<ProductResponse> findAll() {
        // N+1 방지: seller, category를 fetch join으로 한 번에 조회
        return productRepository.findAllWithSellerAndCategory()
                .stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    // 제품 상세 조회
    @Transactional  // 조회수 증가를 위해 @Transactional 필요
    public ProductDetailResponse findById(Long productId, User currentUser) {
        return getDetailInternal(productId, currentUser, true);
    }

    // 제품 상세 조회 (제품 정보 수정, 좋아요 요청 시 사용)
    // 제품 정보 수정, 좋아요, 좋아요 취소 -> 조회수가 증가하지 않아야 함.
    @Transactional(readOnly = true)
    public ProductDetailResponse getDetail(Long productId, User currentUser) {
        return getDetailInternal(productId, currentUser, false);
    }
    
    @Transactional
    public ProductDetailResponse update(
        Long productId, ProductUpdateRequest request, User currentUser
    ) {
        // 1. 로그인 체크 (테스트용: 로그인 시스템 없을 시 샘플 유저 사용)
        if (currentUser == null) {
            currentUser = userRepository.findById(1L)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        }

        // 실제 로그인 체크는 아래 if문 사용 (인증 시스템 구현 후)
        // if (currentUser == null) {
        //     throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        // }
        
        // 2. Product 조회
        Product foundProduct = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 3. 권한 확인 (판매자 본인만 수정 가능)
        if (!foundProduct.isOwnedBy(currentUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 상품만 수정할 수 있습니다");
        }
        
        // 4. 카테고리 업데이트
        Category category = null;
        if (request.hasCategoryId()) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다"));
        }
        
        // 5. 상품 정보 업데이트 (종료일 검증은 Product.update() 내부에서)
        try {
            foundProduct.update(
                request.getTitle(),
                request.getDescription(),
                category,
                request.getEndDate(),
                request.getThumbnailImageUrl()
            );
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, e.getMessage());
        }
        
        // 6. 업데이트된 상품 반환
        return getDetail(productId, currentUser);
    }

    @Transactional
    public void delete(Long productId, User currentUser) {
        // 1. 로그인 체크 (테스트용: 로그인 시스템 없을 시 샘플 유저 사용)
        if (currentUser == null) {
            currentUser = userRepository.findById(1L)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        }

        // 실제 로그인 체크는 아래 if문 사용 (인증 시스템 구현 후)
        // if (currentUser == null) {
        //     throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        // }
        
        // 2. Product 조회
        Product foundProduct = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 3. 권한 확인
        if (!foundProduct.isOwnedBy(currentUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 상품만 삭제할 수 있습니다");
        }
        
        // 4. 펀딩 진행 중이면 삭제 불가
        if (foundProduct.isOngoing()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "진행 중인 펀딩은 삭제할 수 없습니다");
        }
        
        // 5. 삭제
        productRepository.delete(foundProduct);
    }

    private ProductDetailResponse getDetailInternal(
        Long productId,
        User currentUser,
        boolean increaseViewCount
    ) {
        // 1. Product 조회 (N+1 방지: seller, category fetch join)
        Product foundProduct = productRepository.findByIdWithSellerAndCategory(productId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 2. 조회수 증가
        if (increaseViewCount) {
            foundProduct.increaseViewCount();
        }

        // 3. 좋아요 수 조회
        Long likeCount = productLikeRepository.countByProductId(productId);

        // 4. 현재 사용자의 좋아요 여부 확인
        boolean isLiked = false;
        if (currentUser != null) {
            isLiked = productLikeRepository.existsByUserIdAndProductId(currentUser.getId(), productId);
        }

        // 5. ProductDetailResponse 반환
        return ProductDetailResponse.from(foundProduct, likeCount, isLiked);
    }

}
