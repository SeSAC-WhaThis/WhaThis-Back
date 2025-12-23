package com.example.whathis.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 모든 필드는 선택적
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewUpdateRequest {

    @Pattern(
        regexp = "^(https?://.*\\.(png|jpg|jpeg|webp))$",
        message = "이미지 URL(jpg, jpeg, png, webp) 형식이어야 합니다."
    )
    private String imageUrls;

    @Size(min = 1, max = 200, message = "리뷰는 1~200자 사이여야 합니다.")
    private String content;

    @Min(1) @Max(5)
    private Integer star;

}
