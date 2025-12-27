package com.example.whathis.category.controller;

import com.example.whathis.category.dto.response.CategoryResponse;
import com.example.whathis.category.service.CategoryService;
import com.example.whathis.common.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> findAllCategories() {
        List<CategoryResponse> responses = categoryService.findAll();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

}
