package com.example.whathis.productlike.repository;

import com.example.whathis.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductLikeRepository extends JpaRepository<Product, Long> {

}
