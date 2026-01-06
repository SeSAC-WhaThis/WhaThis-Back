package com.example.whathis.review.service;

import com.example.whathis.common.exception.BusinessException;
import com.example.whathis.common.exception.ErrorCode;
import com.example.whathis.product.entity.Product;
import com.example.whathis.product.repository.ProductRepository;
import com.example.whathis.review.dto.request.ReviewCreateRequest;
import com.example.whathis.review.dto.request.ReviewUpdateRequest;
import com.example.whathis.review.dto.response.ReviewResponse;
import com.example.whathis.review.entity.Review;
import com.example.whathis.review.repository.ReviewRepository;
import com.example.whathis.user.entity.User;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.whathis.user.repository.UserRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewResponse save(
        Long productId, User currentUser, ReviewCreateRequest request
    ) {
        // 1. 로그인 체크
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // 유저 정보 재조회 (영속성 컨텍스트 관리)
        User foundUser = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. Product 존재 확인
        Product foundProduct = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 3. 기대평 작성 신청 기간 확인 (펀딩 기간 내에만 가능)
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(foundProduct.getStartDate()) || now.isAfter(foundProduct.getEndDate())) {
            throw new BusinessException(ErrorCode.FUNDING_NOT_ONGOING);
        }

        // 4. 이미 리뷰를 작성했는지 확인 (상품 당 하나의 리뷰만 가능)
        if (reviewRepository.existsByProductIdAndUserId(productId, foundUser.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_REVIEWED);
        }

        // 5. Review 생성 및 저장
        Review review = Review.of(request, foundUser, foundProduct);
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
