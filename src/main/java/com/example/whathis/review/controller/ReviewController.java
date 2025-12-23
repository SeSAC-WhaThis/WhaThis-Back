package com.example.whathis.review.controller;

import com.example.whathis.common.response.ApiResponse;
import com.example.whathis.config.CustomUserDetails;
import com.example.whathis.review.dto.request.ReviewCreateRequest;
import com.example.whathis.review.dto.request.ReviewUpdateRequest;
import com.example.whathis.review.dto.response.ReviewResponse;
import com.example.whathis.review.service.ReviewService;
import com.example.whathis.user.entity.User;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products/{productId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 생성(저장)
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> saveReview(
        @PathVariable Long productId,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ReviewCreateRequest request
    ) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        ReviewResponse response = reviewService.save(productId, currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // 특정 상품의 모든 리뷰 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> findAllReviews(
        @PathVariable Long productId
    ) {
        List<ReviewResponse> responses = reviewService.findAll(productId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 리뷰 수정
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
        @PathVariable Long productId, @PathVariable Long reviewId,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ReviewUpdateRequest request
    ) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        ReviewResponse response = reviewService.update(productId, reviewId, currentUser, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReviewById(
        @PathVariable Long productId, @PathVariable Long reviewId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        reviewService.delete(productId, reviewId, currentUser);
        return ResponseEntity.noContent().build();
    }

}
