package com.example.whathis.review.service;

import com.example.whathis.common.exception.BusinessException;
import com.example.whathis.common.exception.ErrorCode;
import com.example.whathis.common.order.OrderStatus;
import com.example.whathis.order.entity.Order;
import com.example.whathis.order.repository.OrderRepository;
import com.example.whathis.product.entity.Product;
import com.example.whathis.product.repository.ProductRepository;
import com.example.whathis.review.dto.request.ReviewCreateRequest;
import com.example.whathis.review.dto.request.ReviewUpdateRequest;
import com.example.whathis.review.dto.response.ReviewResponse;
import com.example.whathis.review.entity.Review;
import com.example.whathis.review.repository.ReviewRepository;
import com.example.whathis.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    public ReviewResponse save(
        Long productId, User currentUser, ReviewCreateRequest request
    ) {
        // 로그인 체크
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // 2. Product 존재 확인
        Product foundProduct = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 3. Order 존재 확인
        Order foundOrder = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 4. 본인의 주문인지 확인
        if (!foundOrder.getBuyer().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NOT_YOUR_ORDER);
        }

        // 5. 주문한 상품이 맞는지 확인
        if (!foundOrder.getProduct().getId().equals(foundProduct.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        // 6. 주문이 확정된 상태인지 확인 (리뷰 작성 가능 상태)
        if (foundOrder.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_CONFIRMED);
        }

        // 7. 이미 리뷰를 작성했는지 확인 (한 주문당 하나의 리뷰만 가능)
        if (reviewRepository.existsByOrderId(request.getOrderId())) {
            throw new BusinessException(ErrorCode.ALREADY_REVIEWED);
        }

        // 8. Review 생성 및 저장
        Review review = Review.of(request, currentUser, foundProduct, foundOrder);
        Review savedReview = reviewRepository.save(review);

        return ReviewResponse.from(savedReview);
    }

    // 특정 상품의 모든 리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewResponse> findAll(Long productId) {
        // Product 존재 확인
        productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 리뷰 조회 (N+1 방지를 위한 fetch join)
        List<Review> reviews = reviewRepository.findAllByProductIdWithUserAndOrder(productId);

        return reviews.stream()
                .map(ReviewResponse::from)
                .toList();
    }

    // 리뷰 수정
    public ReviewResponse update(
        Long productId, Long reviewId,
        User currentUser, ReviewUpdateRequest request
    ) {
        // 로그인 체크
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
    }

        // 상품 존재 확인
        Product foundProduct = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // Review 존재 확인
        Review foundReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        // 해당 상품의 리뷰인지 확인
        if (!foundReview.getProduct().getId().equals(foundProduct.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        // 본인이 작성한 리뷰인지 확인
        if (!foundReview.isOwnedBy(currentUser)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_REVIEW);
        }

        // 5. 리뷰 수정
        foundReview.update(request.getContent(), request.getStar(), request.getImageUrls());

        return ReviewResponse.from(foundReview);
    }

    // 리뷰 삭제
    public void delete(
        Long productId, Long reviewId, User currentUser
    ) {
        // 로그인 체크
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        Product foundProduct = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // Review 존재 확인
        Review foundReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        // 해당 상품의 리뷰인지 확인
        if (!foundReview.getProduct().getId().equals(foundProduct.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        // 본인이 작성한 리뷰인지 확인
        if (!foundReview.isOwnedBy(currentUser)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_REVIEW);
        }

        // 리뷰 삭제
        reviewRepository.delete(foundReview);
    }

}
