package com.example.whathis.aiSupport.search.tools;

import com.example.whathis.aiSupport.search.dto.SearchDTOs;
import com.example.whathis.category.dto.response.CategoryResponse;
import com.example.whathis.category.service.CategoryService;
import com.example.whathis.product.dto.response.ProductResponse;
import com.example.whathis.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class SearchTools {

    private static final int DEFAULT_LIMIT = 24;
    private static final int MAX_LIMIT = 50;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ObjectMapper objectMapper;


    @Tool(description = "자연어 검색 조건을 바탕으로 상품 목록을 검색하고, 결과를 JSON 배열로 반환합니다. 반드시 이 도구를 사용해 실제 데이터를 조회하세요.")
    public String searchProducts(SearchDTOs.ProductSearchQuery query) {
        if (query == null) {
            return "[]";
        }

        Long categoryId = resolveCategoryIdOrNull(query.categoryName());
        List<ProductResponse> products = productService.findAll(categoryId, null);
        Stream<ProductResponse> stream = products.stream();

        LocalDateTime now = LocalDateTime.now();
        stream = stream.filter(p -> isOngoing(p, now));

        if (query.keyword() != null && !query.keyword().isBlank()) {
            String keyword = query.keyword().trim().toLowerCase(Locale.KOREAN);
            stream = stream.filter(p -> containsIgnoreCase(p.getTitle(), keyword) ||
                containsIgnoreCase(p.getDescription(), keyword));
        }

        if (query.minPrice() != null) {
            int min = query.minPrice();
            stream = stream.filter(p -> p.getPrice() != null && p.getPrice().intValue() >= min);
        }

        if (query.maxPrice() != null) {
            int max = query.maxPrice();
            stream = stream.filter(p -> p.getPrice() != null && p.getPrice().intValue() <= max);
        }

        stream = applySort(stream, query.sort());

        int limit = normalizeLimit(query.limit());
        List<ProductResponse> result = stream.limit(limit).toList();

        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    @Tool(description = "가능한 카테고리 목록을 JSON 배열로 반환합니다. 카테고리명 선택이 어려울 때 사용합니다.")
    public String listCategories() {
        List<CategoryResponse> categories = categoryService.findAll();
        try {
            return objectMapper.writeValueAsString(categories);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    // ------ 편의 메소드
    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private boolean containsIgnoreCase(String value, String keywordLower) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.toLowerCase(Locale.KOREAN).contains(keywordLower);
    }

    private Stream<ProductResponse> applySort(Stream<ProductResponse> stream, String sort) {
        if (sort == null || sort.isBlank()) {
            return stream;
        }

        return switch (sort) {
            case "achievementRate_desc" -> stream.sorted(
                Comparator.comparing(ProductResponse::getAchievementRate, Comparator.nullsLast(Double::compareTo)).reversed());
            case "price_asc" -> stream.sorted(
                Comparator.comparing(p -> p.getPrice() != null ? p.getPrice().intValue() : Integer.MAX_VALUE));
            case "price_desc" -> stream.sorted(
                Comparator.comparing((ProductResponse p) -> p.getPrice() != null ? p.getPrice().intValue() : Integer.MIN_VALUE).reversed());
            case "daysLeft_asc" ->
                stream.sorted(Comparator.comparing(ProductResponse::getDaysLeft, Comparator.nullsLast(Long::compareTo)));
            default -> stream;
        };
    }

    private Long resolveCategoryIdOrNull(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return null;
        }
        String keyword = categoryName.trim();
        List<CategoryResponse> categories = categoryService.findAll();

        List<CategoryResponse> exactMatches = categories.stream()
            .filter(c -> Objects.equals(c.getName(), keyword))
            .toList();
        if (exactMatches.size() == 1) {
            return exactMatches.getFirst().getId();
        }

        List<CategoryResponse> containsMatches = categories.stream()
            .filter(c -> c.getName() != null && c.getName().contains(keyword))
            .toList();
        if (containsMatches.size() == 1) {
            return containsMatches.getFirst().getId();
        }
        return null;
    }

    private boolean isOngoing(ProductResponse product, LocalDateTime now) {
        if (product == null || product.getStartDate() == null || product.getEndDate() == null) {
            return false;
        }
        return !now.isBefore(product.getStartDate()) && !now.isAfter(product.getEndDate());
    }
}
