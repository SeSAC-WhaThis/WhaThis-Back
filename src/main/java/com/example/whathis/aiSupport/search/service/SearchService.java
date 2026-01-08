package com.example.whathis.aiSupport.search.service;

import com.example.whathis.aiSupport.search.dto.SearchDTOs;
import com.example.whathis.product.dto.response.ProductResponse;
import com.example.whathis.product.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SearchService {

    private final ChatClient searchChatClient;
    private final ObjectMapper objectMapper;
    private final ProductService productService;

    public SearchService(
        @Qualifier("searchChatClient") ChatClient searchChatClient,
        ObjectMapper objectMapper,
        ProductService productService
    ) {
        this.searchChatClient = searchChatClient;
        this.objectMapper = objectMapper;
        this.productService = productService;
    }

    public List<ProductResponse> searchProducts(SearchDTOs.SearchRequest request) {
        String query = request != null ? request.query() : null;
        if (query == null || query.isBlank()) {
            return onlyOngoing(productService.findAll(null));
        }

        String content = searchChatClient.prompt()
            .user(query)
            .call()
            .content();

        List<ProductResponse> parsed = tryParseProducts(content);
        if (parsed != null) {
            return onlyOngoing(parsed);
        }

        // fallback: 제목/설명/카테고리명에 포함된 항목만
        List<ProductResponse> all = onlyOngoing(productService.findAll(null));
        String keyword = query.trim();
        return all.stream()
            .filter(p -> contains(p.getTitle(), keyword) ||
                contains(p.getDescription(), keyword) ||
                (p.getCategory() != null && contains(p.getCategory().getName(), keyword)))
            .toList();
    }

    private List<ProductResponse> onlyOngoing(List<ProductResponse> products) {
        if (products == null || products.isEmpty()) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now();
        return products.stream()
            .filter(p -> p != null && p.getStartDate() != null && p.getEndDate() != null)
            .filter(p -> !now.isBefore(p.getStartDate()) && !now.isAfter(p.getEndDate()))
            .toList();
    }

    private boolean contains(String value, String keyword) {
        if (value == null || value.isBlank() || keyword == null || keyword.isBlank()) {
            return false;
        }
        return value.contains(keyword);
    }

    private List<ProductResponse> tryParseProducts(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }

        String jsonArray = extractJsonArrayOrNull(content);
        String candidate = (jsonArray != null) ? jsonArray : content;

        try {
            return objectMapper.readValue(candidate, new TypeReference<>() {
            });
        } catch (Exception ignored) {
            return null;
        }
    }

    private String extractJsonArrayOrNull(String content) {
        int start = content.indexOf('[');
        int end = content.lastIndexOf(']');
        if (start < 0 || end < 0 || end <= start) {
            return null;
        }
        return content.substring(start, end + 1).trim();
    }
}
