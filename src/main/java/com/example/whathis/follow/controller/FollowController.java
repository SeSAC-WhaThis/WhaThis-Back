package com.example.whathis.follow.controller;

import com.example.whathis.common.response.ApiResponse;
import com.example.whathis.common.response.PageResponse;
import com.example.whathis.config.CustomUserDetails;
import com.example.whathis.follow.dto.response.FollowListResponse;
import com.example.whathis.follow.service.FollowService;
import com.example.whathis.product.dto.response.ProductResponse;
import com.example.whathis.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follows")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> follow(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long targetUserId
    ) {
        followService.follow(userDetails.getUser(), targetUserId);
        return ResponseEntity.ok(ApiResponse.successWithMessage("팔로우 되었습니다."));
    }

    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long targetUserId
    ) {
        followService.unfollow(userDetails.getUser(), targetUserId);
        return ResponseEntity.ok(ApiResponse.successWithMessage("언팔로우 되었습니다."));
    }

    // 내가 팔로우한 사용자 목록 + count
    @GetMapping("/followings")
    public ResponseEntity<ApiResponse<FollowListResponse>> getFollowings(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FollowListResponse response = followService.getFollowings(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 나를 팔로우한 사용자 목록 + count
    @GetMapping("/followers")
    public ResponseEntity<ApiResponse<FollowListResponse>> getFollowers(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FollowListResponse response = followService.getFollowers(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getFollowerProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> productPage = followService.getProductByFollowerId(userDetails.getUser(), pageable);
        PageResponse<ProductResponse> response = PageResponse.of(productPage);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
