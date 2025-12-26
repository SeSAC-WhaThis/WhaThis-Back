package com.example.whathis.auth.controller;

import com.example.whathis.auth.dto.request.KakaoLoginRequest;
import com.example.whathis.auth.dto.request.LoginRequest;
import com.example.whathis.auth.dto.request.SignupRequest;
import com.example.whathis.auth.dto.response.TokenResponse;
import com.example.whathis.auth.service.AuthService;
import com.example.whathis.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.Token;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.successWithMessage("회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        TokenResponse response = authService.login(request);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + response.getAccessToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.successWithMessage("로그인이 완료되었습니다."));
    }

    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoLogin(
        @Valid @RequestBody KakaoLoginRequest request
    ) {
        TokenResponse response = authService.kakaoLogin(request.getCode());
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + response.getAccessToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.successWithMessage("카카오 로그인이 완료되었습니다."));
    }
}