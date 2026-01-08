package com.example.whathis.aiSupport.search.controller;

import com.example.whathis.aiSupport.search.dto.SearchDTOs;
import com.example.whathis.aiSupport.search.service.SearchService;
import com.example.whathis.common.response.ApiResponse;
import com.example.whathis.product.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> search(@RequestBody SearchDTOs.SearchRequest request) {
        List<ProductResponse> results = searchService.searchProducts(request);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}

