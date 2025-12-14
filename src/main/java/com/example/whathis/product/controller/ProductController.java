package com.example.whathis.product.controller;

import com.example.whathis.product.service.ProductService;
import com.example.whathis.productlike.service.ProductLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ProductLikeService productLikeService;

}
