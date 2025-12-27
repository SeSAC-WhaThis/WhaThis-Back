package com.example.whathis.category.service;

import com.example.whathis.category.dto.response.CategoryResponse;
import com.example.whathis.category.entity.Category;
import com.example.whathis.category.repository.CategoryRepository;
import com.example.whathis.product.dto.response.ProductResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> findAll() {
        List<Category> categoryList = categoryRepository.findAll();
        return categoryList.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

}
