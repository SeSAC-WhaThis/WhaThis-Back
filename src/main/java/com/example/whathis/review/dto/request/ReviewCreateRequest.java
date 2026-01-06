package com.example.whathis.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {

    @Pattern(regexp = "^(https?://.*\\.(png|jpg|jpeg|webp))$", message = "이미지 URL(jpg, jpeg, png, webp) 형식이어야 합니다.")
    private String imageUrls;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(min = 1, max = 200, message = "리뷰는 1~200자 사이여야 합니다.")
    private String content;

    @NotNull(message = "평점은 필수입니다.")
    @Min(1)
    @Max(5)
    private Integer star;

}
