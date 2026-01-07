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

    public List<ProductResponse> getProductByFollowerId(User currentUser) {
        List<ProductResponse> responses = productRepository.findProductsByFollowerId(currentUser.getId())
                .stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());

        for (ProductResponse response : responses) {
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
