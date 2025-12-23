package com.example.whathis.user.service;

import com.example.whathis.auth.dto.request.PasswordUpdateRequest;
import com.example.whathis.auth.dto.request.UserUpdateRequest;
import com.example.whathis.common.exception.BusinessException;
import com.example.whathis.common.exception.ErrorCode;
import com.example.whathis.config.JwtProvider;
import com.example.whathis.auth.dto.request.LoginRequest;
import com.example.whathis.auth.dto.request.SignupRequest;
import com.example.whathis.auth.dto.response.TokenResponse;
import com.example.whathis.user.dto.response.UserResponse;
import com.example.whathis.user.entity.User;
import com.example.whathis.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getProfile(User user) {
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(User user, UserUpdateRequest request) {
        if(request.getNickname() != null &&
                !user.getNickname().equals(request.getNickname()) &&
                userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        currentUser.updateProfile(
                request.getName(),
                request.getNickname(),
                request.getPhoneNumber(),
                request.getAddress(),
                request.getProfileImageUrl()
        );

        return UserResponse.from(currentUser);
    }

    @Transactional
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

    @Transactional
    public void deleteUser(User user) {
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 진행중인 펀딩이나 주문이 있는지 확인하는 로직은 추후에 추가
        userRepository.delete(currentUser);
    }
}
