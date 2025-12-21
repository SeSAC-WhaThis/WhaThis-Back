package com.example.whathis.user.dto.response;

import com.example.whathis.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String phoneNumber;
    private String address;
    private String profileImageUrl;
    private String brn;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .profileImageUrl(user.getProfileImageUrl())
                .brn(user.getBrn())
                .build();
    }
}
