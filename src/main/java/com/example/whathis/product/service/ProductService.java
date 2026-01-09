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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductLikeRepository productLikeRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProductDetailResponse save(
        ProductCreateRequest request,
        User currentUser
    ) {
        // 1. 유저 로그인 체크
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

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

        // 5. 이미지 업로드 처리
        String thumbnailImageUrl = uploadFile(request.getThumbnailImageUrl());
        String storyImageUrl = uploadFile(request.getStoryImageUrl());

        // 6. Product 생성
        Product product = Product.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .seller(currentUser)
                .category(category)
                .price(request.getPrice())
                .goalAmount(request.getGoalAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .thumbnailImageUrl(thumbnailImageUrl)
                .storyImageUrl(storyImageUrl)
                .inventory(request.getInventory())
                .build();

        // 7. Product 저장
        Product savedProduct = productRepository.save(product);

        // 8. ProductDetailResponse 반환 (생성 시 좋아요 0, 좋아요 여부 false)
        return ProductDetailResponse.from(savedProduct, 0L, false);
    }

    // 파일 업로드 처리 (로컬 저장소)
    private String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 저장 디렉토리 설정 (프로젝트 루트의 uploads 폴더)
            String uploadDir = "uploads";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 고유 파일명 생성
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // 파일 저장
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 반환할 URL (정적 리소스 경로)
            return "/uploads/" + fileName;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류가 발생했습니다.");
        }
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

    // 제품 전체 조회 (또는 카테고리별 조회)
    public List<ProductResponse> findAll(
        Long categoryId,
        User currentUser
    ) {
        // categoryId가 있으면 카테고리별 조회, 없으면 전체 조회
        List<Product> products;
        if (categoryId != null) {
            products = productRepository.findAllByCategoryId(categoryId);
        } else {
            // N+1 방지: seller, category를 fetch join으로 한 번에 조회
            products = productRepository.findAllWithSellerAndCategory();
        }

        List<ProductResponse> responses = products.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());

        // 좋아요 상태와 수 설정
        for (ProductResponse response : responses) {
            Long likeCount = productLikeRepository.countByProductId(response.getId());
            response.setLikeCount(likeCount);

            boolean isLiked = false;
            if (currentUser != null) {
                isLiked = productLikeRepository.existsByUserIdAndProductId(currentUser.getId(), response.getId());
            }
            response.setIsLiked(isLiked);
        }

        return responses;
    }

    // 제품 전체 조회 (또는 카테고리별 조회) - 페이징 지원
    public Page<ProductResponse> findAll(
            Long categoryId,
            User currentUser,
            Pageable pageable) {
        // categoryId가 있으면 카테고리별 조회, 없으면 전체 조회
        Page<Product> products;
        if (categoryId != null) {
            products = productRepository.findAllByCategoryId(categoryId, pageable);
        } else {
            // N+1 방지: seller, category를 fetch join으로 한 번에 조회
            products = productRepository.findAllWithSellerAndCategory(pageable);
        }

        Page<ProductResponse> responses = products.map(ProductResponse::from);

        // 좋아요 상태와 수 설정
        for (ProductResponse response : responses.getContent()) {
            Long likeCount = productLikeRepository.countByProductId(response.getId());
            response.setLikeCount(likeCount);

            boolean isLiked = false;
            if (currentUser != null) {
                isLiked = productLikeRepository.existsByUserIdAndProductId(currentUser.getId(), response.getId());
            }
            response.setIsLiked(isLiked);
        }

        return responses;
    }

    // 내가 등록한 제품 전체 조회
    public List<ProductResponse> findAllMyProducts(User currentUser) {
        // 유저 로그인 체크
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        return productRepository.findAllSellingProducts(currentUser.getId())
                .stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    // 내가 등록한 제품 전체 조회 - 페이징
    public Page<ProductResponse> findAllMyProducts(User currentUser, Pageable pageable) {
        // 유저 로그인 체크
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        return productRepository.findAllSellingProducts(currentUser.getId(), pageable)
                .map(ProductResponse::from);
    }

    // 특정 유저가 생성한 제품 전체 조회 (유저 ID 기반)
    public List<ProductResponse> findAllProductsByUserId(Long userId) {
        // 유저 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        return productRepository.findAllSellingProducts(userId)
                .stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    // 제품 상세 조회
    @Transactional // 조회수 증가를 위해 @Transactional 필요
    public ProductDetailResponse findById(
        Long productId,
        User currentUser
    ) {
        return getDetailInternal(productId, currentUser, true);
    }

    // 제품 상세 조회 (제품 정보 수정, 좋아요 요청 시 사용)
    // 제품 정보 수정, 좋아요, 좋아요 취소 -> 조회수가 증가하지 않아야 함.
    public ProductDetailResponse getDetail(
        Long productId,
        User currentUser
    ) {
        return getDetailInternal(productId, currentUser, false);
    }

    @Transactional
    public ProductDetailResponse update(
        Long productId,
        ProductUpdateRequest request,
        User currentUser
    ) {
        // 1. 로그인 체크
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

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
            String thumbnailImageUrl = null;
            if (request.hasThumbnailImageUrl()) {
                thumbnailImageUrl = uploadFile(request.getThumbnailImageUrl());
            }

            String storyImageUrl = null;
            if (request.hasStoryImageUrl()) {
                storyImageUrl = uploadFile(request.getStoryImageUrl());
            }

            foundProduct.update(
                    request.getTitle(),
                    request.getDescription(),
                    category,
                    request.getEndDate(),
                    thumbnailImageUrl,
                    storyImageUrl,
                    request.getPrice(),
                    request.getGoalAmount(),
                    request.getInventory());

        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, e.getMessage());
        }

        // 6. 업데이트된 상품 반환
        return getDetail(productId, currentUser);
    }

    @Transactional
    public void delete(
        Long productId,
        User currentUser
    ) {
        // 1. 로그인 체크
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // 2. Product 조회
        Product foundProduct = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 3. 권한 확인
        if (!foundProduct.isOwnedBy(currentUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 상품만 삭제할 수 있습니다");
        }

        productLikeRepository.deleteAllByProduct(foundProduct);
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
