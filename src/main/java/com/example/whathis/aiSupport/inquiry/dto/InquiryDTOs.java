package com.example.whathis.aiSupport.inquiry.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.math.BigDecimal;

public class InquiryDTOs {

    public record FundProductRequest(
        @JsonPropertyDescription("상품 제목 (최대 40자). 예: \"초소형 휴대용 블렌더\"") String title,
        @JsonPropertyDescription("상품 설명 (최대 500자). 예: \"언제 어디서나 간편하게 스무디를 만들 수 있는 휴대용 블렌더입니다.\"") String description,
        @JsonPropertyDescription("카테고리명 (예: 전자제품). 보통 categoryName만 입력하면 자동으로 ID가 선택됩니다.") String categoryName,
        @JsonPropertyDescription("카테고리 ID (모르면 null). categoryName이 애매할 때만 사용") Long categoryId,
        @JsonPropertyDescription("펀딩 시작일 (예: 2026년 1월 12일 또는 오늘)") String startDate,
        @JsonPropertyDescription("펀딩 종료일 (예: 2026년 2월 12일 또는 오늘)") String endDate,
        @JsonPropertyDescription("상품 가격(원)") BigDecimal price,
        @JsonPropertyDescription("펀딩 목표 금액(원)") BigDecimal goalAmount,
        @JsonPropertyDescription("썸네일 이미지 URL (주소.jpg|jpeg|png|webp)") String thumbnailImageUrl,
        @JsonPropertyDescription("스토리 이미지 URL (주소.jpg|jpeg|png|webp)") String storyImageUrl,
        @JsonPropertyDescription("재고 수량 (0보다 커야 함)") Integer inventory,
        @JsonPropertyDescription("사업자등록번호 (예: 123-45-67890). 이미 사용자에게 등록되어 있으면 null 가능") String brn) {
    }

    public record RecentlyViewedProductsRequest(
        @JsonPropertyDescription("조회할 최근 본 상품 개수 (최대 10 권장)") Integer limit) {
    }

    public record RecommendFromRecentlyViewedRequest(
        @JsonPropertyDescription("추천받을 상품 개수 (예: 5)") Integer topK,
        @JsonPropertyDescription("최대 예산 (원). 제한 없으면 null") BigDecimal maxBudget,
        @JsonPropertyDescription("특정 카테고리로 제한하고 싶으면 카테고리명 (예: 전자제품). 제한 없으면 null") String categoryName) {
    }

    public record ProductReviewsRequest(
        @JsonPropertyDescription("리뷰를 조회할 상품 ID") Long productId) {
    }

    public record SellerProfileRequest(
        @JsonPropertyDescription("조회할 판매자(유저) ID") Long sellerId) {
    }

}
