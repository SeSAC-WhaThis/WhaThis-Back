package com.example.whathis.product.controller;

import com.example.whathis.common.response.ApiResponse;
import com.example.whathis.common.response.PageResponse;
import com.example.whathis.config.CustomUserDetails;
import com.example.whathis.product.dto.request.ProductCreateRequest;
import com.example.whathis.product.dto.request.ProductUpdateRequest;
import com.example.whathis.product.dto.response.ProductDetailResponse;
import com.example.whathis.product.dto.response.ProductResponse;
import com.example.whathis.product.service.ProductService;
import com.example.whathis.productlike.service.ProductLikeService;
import com.example.whathis.user.entity.User;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ProductLikeService productLikeService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetailResponse>> saveProduct(
            @Valid @ModelAttribute ProductCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        ProductDetailResponse response = productService.save(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "제품이 성공적으로 등록되었습니다."));
    }

    // 상품 목록 조회 (필터링: categoryId, 페이징: page, size)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> findAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> productPage = productService.findAll(categoryId, currentUser, pageable);
        PageResponse<ProductResponse> response = PageResponse.of(productPage);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 본인 등록 상품 목록 조회
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> findAllMyProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        List<ProductResponse> response = productService.findAllMyProducts(currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 특정 유저가 생성한 상품 목록 조회 (유저 ID 기반)
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> findAllProductsByUserId(
            @PathVariable Long userId
    ) {
        List<ProductResponse> response = productService.findAllProductsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 상품 단일 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> findProductById(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 비로그인 사용자도 조회 가능 (isLiked는 false로 반환)
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        ProductDetailResponse response = productService.findById(productId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 상품 수정
    @PostMapping("/{productId}/update")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @ModelAttribute ProductUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        ProductDetailResponse response = productService.update(productId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response, "제품 정보가 수정되었습니다"));
    }

    // 상품 삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProductById(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        productService.delete(productId, currentUser);
        return ResponseEntity.noContent().build();
    }

    // 제품 좋아요
    @PostMapping("/{productId}/like")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> like(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        ProductDetailResponse response = productLikeService.like(currentUser, productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // 제품 좋아요 취소
    @DeleteMapping("/{productId}/like")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> unlike(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        ProductDetailResponse response = productLikeService.unlike(currentUser, productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 유저의 좋아요 제품만 조회
    @GetMapping("/like")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getMyLikeProducts(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        List<ProductResponse> response = productLikeService.getLikedProducts(currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
