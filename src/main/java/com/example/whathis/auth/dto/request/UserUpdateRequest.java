package com.example.whathis.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    private String name;

    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하입니다.")
    private String nickname;

    private String phoneNumber;
    private String address;
    private String profileImageUrl;
}
