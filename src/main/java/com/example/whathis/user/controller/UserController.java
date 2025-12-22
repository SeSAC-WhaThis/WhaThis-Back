package com.example.whathis.user.controller;

import com.example.whathis.common.response.ApiResponse;
import com.example.whathis.user.dto.request.LoginRequest;
import com.example.whathis.user.dto.request.SignupRequest;
import com.example.whathis.user.dto.response.TokenResponse;
import com.example.whathis.user.dto.response.UserResponse;
import com.example.whathis.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.successWithMessage("회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        TokenResponse response = userService.login(request);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + response.getAccessToken());
        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.successWithMessage("로그인이 완료되었습니다."));
    }
}
