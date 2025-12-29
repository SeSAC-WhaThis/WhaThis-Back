package com.example.whathis.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoLoginRequest {
    @NotBlank(message = "Auth code는 필수입니다.")
    private String code;

}
