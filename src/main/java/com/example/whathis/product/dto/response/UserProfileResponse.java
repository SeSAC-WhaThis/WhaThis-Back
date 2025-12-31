package com.example.whathis.product.dto.response;

import com.example.whathis.user.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class UserProfileResponse {
    // 사용자 정보
    private Long id;
    private String name;
    private String nickname;
    private String profileImageUrl;
    private String brn;

    // 통계 정보
    private Long followerCount;
    private Double ratingAvg;
    private BigDecimal salesTotalAmount;

    // 팔로우 여부
    @JsonProperty("following")
    private boolean isFollowing;

    // 판매 중인 상품 목록
    private List<ProductResponse> products;

    public static UserProfileResponse of(
            User user,
            Long followerCount,
            Double ratingAvg,
            BigDecimal salesTotalAmount,
            boolean isFollowing,
            List<ProductResponse> products

    ) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .brn(user.getBrn())
                .followerCount(followerCount)
                .ratingAvg(ratingAvg)
                .salesTotalAmount(salesTotalAmount)
                .isFollowing(isFollowing)
                .products(products)
                .build();
    }
}
