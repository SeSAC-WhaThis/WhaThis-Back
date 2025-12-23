package com.example.whathis.productlike.service;

import com.example.whathis.common.exception.BusinessException;
import com.example.whathis.common.exception.ErrorCode;
import com.example.whathis.product.dto.response.ProductDetailResponse;
import com.example.whathis.product.entity.Product;
import com.example.whathis.product.repository.ProductRepository;
import com.example.whathis.product.service.ProductService;
import com.example.whathis.productlike.entity.ProductLike;
import com.example.whathis.productlike.repository.ProductLikeRepository;
import com.example.whathis.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductLikeService {

    private final ProductLikeRepository productLikeRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public ProductDetailResponse like(User currentUser, Long productId) {
        // 로그인 체크
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // 상품 존재 여부 확인
        Product foundProduct = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 유저의 상품 좋아요 여부 확인
        // 좋아요 요청을 한 번 더 보내면 발생하는 에러
        if (productLikeRepository.existsByUserIdAndProductId(currentUser.getId(), foundProduct.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_LIKED);
        }

        // 좋아요 추가
        ProductLike productLike = ProductLike.builder()
                                    .user(currentUser)
                                    .product(foundProduct)
                                    .build();

        productLikeRepository.save(productLike);

        // 최신 상품 정보(좋아요 수, isLiked 포함) 반환 (조회수 증가 없이)
        return productService.getDetail(productId, currentUser);
    }

    public ProductDetailResponse unlike(User currentUser, Long productId) {
        // 로그인 체크
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // 상품 존재 여부 확인
        Product foundProduct = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 좋아요를 누른 적이 없으면 에러
        // 좋아요 취소 요청을 한 번 더 보내면 발생하는 에러
        if (!productLikeRepository.existsByUserIdAndProductId(currentUser.getId(), foundProduct.getId())) {
            throw new BusinessException(ErrorCode.NOT_LIKED);
        }

        // 좋아요 삭제
        ProductLike productLike = productLikeRepository.findByUserIdAndProductId(currentUser.getId(), foundProduct.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_LIKED));

        productLikeRepository.delete(productLike);

        // 최신 상품 정보(좋아요 수, isLiked 포함) 반환 (조회수 증가 없이)
        return productService.getDetail(productId, currentUser);
    }

}
