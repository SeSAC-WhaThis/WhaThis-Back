package com.example.whathis.follow.service;

import com.example.whathis.common.exception.BusinessException;
import com.example.whathis.common.exception.ErrorCode;
import com.example.whathis.follow.dto.response.FollowListResponse;
import com.example.whathis.follow.dto.response.FollowUserResponse;
import com.example.whathis.follow.entity.Follow;
import com.example.whathis.follow.repository.FollowRepository;
import com.example.whathis.product.dto.response.ProductResponse;
import com.example.whathis.product.entity.Product;
import com.example.whathis.product.repository.ProductRepository;
import com.example.whathis.productlike.repository.ProductLikeRepository;
import com.example.whathis.user.entity.User;
import com.example.whathis.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductLikeRepository productLikeRepository;

    @Transactional
    public void follow(User currentUser, Long targetUserId) {
        if(currentUser.getId().equals(targetUserId)) {
            throw new BusinessException(ErrorCode.CANNOT_SELF_FOLLOW);
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if(followRepository.existsByFollowerAndFollowing(currentUser, targetUser)) {
            throw new BusinessException(ErrorCode.ALREADY_FOLLOWING);
        }

        Follow follow = Follow.builder()
                .follower(currentUser)
                .following(targetUser)
                .build();

        followRepository.save(follow);
    }

    @Transactional
    public void unfollow(User currentUser, Long targetUserId) {

        Follow follow = followRepository.findByFollowerIdAndFollowingId(currentUser.getId(), targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOLLOWING));

        followRepository.delete(follow);
    }

    public FollowListResponse getFollowings(User currentUser) {
        List<FollowUserResponse> list = followRepository.findFollowingsByFollowerId(currentUser.getId())
                .stream()
                .map(follow -> FollowUserResponse.from(follow.getFollowing()))
                .collect(Collectors.toList());

        return FollowListResponse.of(list);
    }

    public FollowListResponse getFollowers(User currentUser) {
        List<FollowUserResponse> list = followRepository.findFollowersByFollowingId(currentUser.getId())
                .stream()
                .map(follow -> FollowUserResponse.from(follow.getFollower()))
                .collect(Collectors.toList());

        return FollowListResponse.of(list);
    }

    // 팔로우한 사용자의 상품 목록 조회 (페이징 적용)
    public Page<ProductResponse> getProductByFollowerId(User currentUser, Pageable pageable) {
        // 1. 페이징된 Entity 조회
        Page<Product> productPage = productRepository.findProductsByFollowerId(currentUser.getId(), pageable);

        // 2. DTO 변환
        Page<ProductResponse> responses = productPage.map(ProductResponse::from);

        // 3. 좋아요 여부 및 개수 설정 (조회된 페이지 내의 상품들에 대해서만 수행)
        for (ProductResponse response : responses.getContent()) {
            Long likeCount = productLikeRepository.countByProductId(response.getId());
            response.setLikeCount(likeCount);

            boolean isLiked = false;
            if (currentUser != null) {
                isLiked = productLikeRepository.existsByUserIdAndProductId(currentUser.getId(), response.getId());
            }
            response.setIsLiked(isLiked);
        }

        return responses;
    }
}
