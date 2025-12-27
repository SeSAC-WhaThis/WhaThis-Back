package com.example.whathis.auth.service;

import com.example.whathis.auth.AuthProvider;
import com.example.whathis.auth.dto.request.LoginRequest;
import com.example.whathis.auth.dto.request.SignupRequest;
import com.example.whathis.auth.dto.response.KakaoTokenResponse;
import com.example.whathis.auth.dto.response.KakaoUserResponse;
import com.example.whathis.auth.dto.response.TokenResponse;
import com.example.whathis.common.exception.BusinessException;
import com.example.whathis.common.exception.ErrorCode;
import com.example.whathis.config.JwtProvider;
import com.example.whathis.user.entity.User;
import com.example.whathis.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final KakaoOauthService kakaoOauthService;

    @Transactional
    public void signup(SignupRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        if(userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
    }

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

    @Transactional
    public TokenResponse kakaoLogin(String code) {
        // 1. Auth code 이용해서 Access Token 발급
        KakaoTokenResponse tokenResponse = kakaoOauthService.getKakaoToken(code);
        // 2. Access Token 사용자 정보 조회
        KakaoUserResponse userInfo = kakaoOauthService.getUserInfo(tokenResponse.getAccessToken());

        // 3. 기존 카카오 사용자 조회 없으면 새로 가입
        User user = userRepository.findByProviderAndProviderId(
                AuthProvider.KAKAO, String.valueOf(userInfo.getId())
        ).orElseGet(() -> createKakaoUser(userInfo));

        // 4. 프로필 정보 업데이트
        user.updateOAuthProfile(
                userInfo.getKakaoAccount().getProfile().getNickname(),
                userInfo.getKakaoAccount().getProfile().getProfileImageUrl()
        );

        // 5. JWT 발급
        String token = jwtProvider.createToken(user.getEmail());
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }

    // 카카오 신규 사용자 생성
    private User createKakaoUser(KakaoUserResponse userInfo) {
        String kakaoEmail = "kakao_" + userInfo.getId() + "@kakao.com";
        String nickname = userInfo.getKakaoAccount().getProfile().getNickname();
        String providerId = String.valueOf(userInfo.getId());
        String randomPassword = UUID.randomUUID().toString();

        User user = User.builder()
                .email(kakaoEmail)
                .name(nickname)
                .nickname(nickname)
                .password(passwordEncoder.encode(randomPassword))
                .provider(AuthProvider.KAKAO)
                .providerId(providerId)
                .build();

        return userRepository.save(user);
    }
}