package com.example.whathis.user.service;

import com.example.whathis.auth.dto.request.PasswordUpdateRequest;
import com.example.whathis.auth.dto.request.UserUpdateRequest;
import com.example.whathis.common.exception.BusinessException;
import com.example.whathis.common.exception.ErrorCode;
import com.example.whathis.config.JwtProvider;
import com.example.whathis.auth.dto.request.LoginRequest;
import com.example.whathis.auth.dto.request.SignupRequest;
import com.example.whathis.auth.dto.response.TokenResponse;
import com.example.whathis.follow.repository.FollowRepository;
import com.example.whathis.user.dto.response.UserResponse;
import com.example.whathis.user.entity.User;
import com.example.whathis.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FollowRepository followRepository;

    @Transactional(readOnly = true)
    public UserResponse getProfile(User user) {
        return UserResponse.from(user);
    }

    public UserResponse updateProfile(User user, UserUpdateRequest request) {
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.getNickname() != null) {
            if (currentUser.getNickname().equals(request.getNickname())) {
                throw new BusinessException(ErrorCode.SAME_AS_CURRENT_NICKNAME);
            }
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
            }
        }

        currentUser.updateProfile(request);

        return UserResponse.from(currentUser);
    }

    public void updatePassword(User user, PasswordUpdateRequest request) {
        if(!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        if(passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_SAME_AS_OLD);
        }

        if(!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        currentUser.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    public void deleteUser(User user) {
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 사용자 탈퇴 시 Follow 테이블과의 외래 키 제약 조건으로 인해 500 에러 발생
        // -> 사용자를 삭제하기 전에 팔로우/팔로잉 관계를 먼저 삭제
        followRepository.deleteByFollower(currentUser);
        followRepository.deleteByFollowing(currentUser);

        // 진행중인 펀딩이나 주문이 있는지 확인하는 로직은 추후에 추가
        userRepository.delete(currentUser);
    }
}
