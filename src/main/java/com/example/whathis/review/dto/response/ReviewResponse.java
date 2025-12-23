package com.example.whathis.review.dto.response;

import com.example.whathis.review.entity.Review;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewResponse {

    private Long id;

    private String content;

    private Integer star;

    private List<String> imageUrls;

    private Integer helpfulCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
    // 작성자 정보
    private ReviewerInfo reviewer;

    // 주문 정보
    private OrderInfo order;

    // 정적 중첩 클래스 - 중요 정보 보호를 위함
    // 시작
    @Getter
    @Builder
    public static class ReviewerInfo {
        private Long id;
        private String nickname;
        private String profileImageUrl;
    }

    @Getter
    @Builder
    public static class OrderInfo {
        private Long id;
        private String orderNumber;
    }
    // 끝

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .content(review.getContent())
                .star(review.getStar())
                .imageUrls(parseImageUrls(review.getImageUrls()))
                .helpfulCount(review.getHelpfulCount())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .reviewer(ReviewerInfo.builder()
                    .id(review.getUser().getId())
                    .nickname(review.getUser().getNickname())
                    .profileImageUrl(review.getUser().getProfileImageUrl())
                    .build())
                .order(review.getOrder() != null ? OrderInfo.builder()
                    .id(review.getOrder().getId())
                    .orderNumber(review.getOrder().getOrderNumber())
                    .build() : null)
                .build();
    }


    // JSON 배열 형태의 이미지 URL 문자열을 List로 파싱
    // ex) "["url1", "url2"]" -> ["url1", "url2"]
    private static List<String> parseImageUrls(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.isBlank()) {
            return Collections.emptyList();
        }
        
        // 간단한 파싱(실제로는 Jackson 사용)
        String cleaned = imageUrlsJson
            .replace("[", "")
            .replace("]", "")
            .replace("\"", "")
            .trim();
        
        if (cleaned.isEmpty()) {
            return Collections.emptyList();
        }
        
        return Arrays.asList(cleaned.split(",\\s*"));
    }

}
