package com.example.whathis.product.controller;

import com.example.whathis.common.response.ApiResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        ProductDetailResponse response = productService.save(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "제품이 성공적으로 등록되었습니다."));
    }

    // 상품 목록 조회 (필터링: categoryId)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> findAllProducts(
            @RequestParam(required = false) Long categoryId) {
        List<ProductResponse> response = productService.findAll(categoryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 본인 등록 상품 목록 조회
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> findAllMyProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        List<ProductResponse> response = productService.findAllMyProducts(currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 상품 단일 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> findProductById(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 비로그인 사용자도 조회 가능 (isLiked는 false로 반환)
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        ProductDetailResponse response = productService.findById(productId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 상품 수정
    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        ProductDetailResponse response = productService.update(productId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response, "제품 정보가 수정되었습니다"));
    }

    // 상품 삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProductById(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        productService.delete(productId, currentUser);
        return ResponseEntity.noContent().build();
    }

    // 제품 좋아요
    @PostMapping("/{productId}/like")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> like(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        ProductDetailResponse response = productLikeService.like(currentUser, productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // 제품 좋아요 취소
    @DeleteMapping("/{productId}/like")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> unlike(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        ProductDetailResponse response = productLikeService.unlike(currentUser, productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
