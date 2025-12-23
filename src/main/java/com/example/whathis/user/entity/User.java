package com.example.whathis.user.entity;

import com.example.whathis.BaseEntity;
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

    @Builder
    public User(String email, String password, String name, String nickname) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
    }

    // 전화번호, 주소는 주문 시 배송지 정보에 입력
    public void setDeliveryInfo(String phoneNumber, String address) {
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // 회원 정보 수정
    public void updateProfile(String name, String nickname, String phoneNumber, String address, String profileImageUrl) {
        if(name != null) this.name = name;
        if(nickname != null) this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.profileImageUrl = profileImageUrl;
    }

    // 비밀번호 수정
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
