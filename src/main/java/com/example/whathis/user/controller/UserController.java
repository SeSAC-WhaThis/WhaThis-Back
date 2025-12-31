package com.example.whathis.user.controller;

import com.example.whathis.auth.dto.request.PasswordUpdateRequest;
import com.example.whathis.auth.dto.request.UserUpdateRequest;
import com.example.whathis.common.response.ApiResponse;
import com.example.whathis.auth.dto.request.LoginRequest;
import com.example.whathis.auth.dto.request.SignupRequest;
import com.example.whathis.auth.dto.response.TokenResponse;
import com.example.whathis.config.CustomUserDetails;
import com.example.whathis.product.dto.response.UserProfileResponse;
import com.example.whathis.user.dto.response.UserResponse;
import com.example.whathis.user.entity.User;
import com.example.whathis.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponse response = userService.getProfile(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        userService.updateProfile(userDetails.getUser(), request);
        return ResponseEntity.ok(ApiResponse.successWithMessage("회원정보가 수정되었습니다."));
    }

    @PatchMapping("/profile/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordUpdateRequest request
    ) {
        userService.updatePassword(userDetails.getUser(), request);
        return ResponseEntity.ok(ApiResponse.successWithMessage("비밀번호가 변경되었습니다."));
    }

    @DeleteMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.deleteUser(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.successWithMessage("회원 탈퇴가 완료되었습니다."));
    }

    @GetMapping("/profile/{sellerId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getSellerProfile(
            @PathVariable Long sellerId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        UserProfileResponse response = userService.getUserProfile(sellerId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
