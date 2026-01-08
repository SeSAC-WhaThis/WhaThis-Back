package com.example.whathis.aiSupport.search.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class SearchDTOs {

    public record SearchRequest(
        @JsonPropertyDescription("사용자가 입력한 검색 문장/키워드 (예: '가전 3만원 이하 블렌더')") String query) {
    }

    public record ProductSearchQuery(
        @JsonPropertyDescription("검색 키워드 (상품명/설명에서 검색). 없으면 null") String keyword,
        @JsonPropertyDescription("카테고리명 (예: 가전·디지털). 없으면 null") String categoryName,
        @JsonPropertyDescription("최소 가격(원). 예: 50000. 없으면 null") Integer minPrice,
        @JsonPropertyDescription("최대 가격(원). 예: 30000. 없으면 null") Integer maxPrice,
        @JsonPropertyDescription("정렬 기준 (achievementRate_desc | price_asc | price_desc | daysLeft_asc). 없으면 null") String sort,
        @JsonPropertyDescription("반환할 최대 개수 (예: 24). 없으면 24") Integer limit) {
    }
}
