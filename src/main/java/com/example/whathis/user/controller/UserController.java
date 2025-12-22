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
    public ResponseEntity<ApiResponse<UserResponse>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        UserResponse response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        TokenResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
