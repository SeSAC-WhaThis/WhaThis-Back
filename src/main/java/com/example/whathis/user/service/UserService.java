package com.example.whathis.user.service;

import com.example.whathis.common.exception.BusinessException;
import com.example.whathis.common.exception.ErrorCode;
import com.example.whathis.config.JwtProvider;
import com.example.whathis.user.dto.request.LoginRequest;
import com.example.whathis.user.dto.request.SignupRequest;
import com.example.whathis.user.dto.response.TokenResponse;
import com.example.whathis.user.dto.response.UserResponse;
import com.example.whathis.user.entity.User;
import com.example.whathis.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.Token;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider  jwtProvider;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        // 이메일 중복 체크
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 닉네임 중복 체크
        if(userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        String token = jwtProvider.createToken(user.getEmail());

        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }
}
