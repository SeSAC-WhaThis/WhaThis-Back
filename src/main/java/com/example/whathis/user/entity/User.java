package com.example.whathis.user.entity;

import com.example.whathis.BaseEntity;
import com.example.whathis.auth.dto.request.UserUpdateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.example.whathis.auth.AuthProvider;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String nickname;

    private String phoneNumber;

    private String address;

    @Setter
    private String profileImageUrl;

    @Setter
    private String brn;

    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL;

    private String providerId;

    @Builder
    public User(String email, String password, String name, String nickname, AuthProvider provider, String providerId) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.provider = provider != null ? provider : AuthProvider.LOCAL;
        this.providerId = providerId;
    }

    // 전화번호, 주소는 주문 시 배송지 정보에 입력
    public void setDeliveryInfo(String phoneNumber, String address) {
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // 회원 정보 수정
    public void updateProfile(UserUpdateRequest request) {
        if (request.getName() != null) this.name = request.getName();
        if (request.getNickname() != null) this.nickname = request.getNickname();
        if (request.getPhoneNumber() != null) this.phoneNumber = request.getPhoneNumber();
        if (request.getAddress() != null) this.address = request.getAddress();
        if (request.getProfileImageUrl() != null) this.profileImageUrl = request.getProfileImageUrl();
    }

    // 비밀번호 수정
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateOAuthProfile(String name, String profileImageUrl) {
        if (name != null) this.name = name;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }
}
