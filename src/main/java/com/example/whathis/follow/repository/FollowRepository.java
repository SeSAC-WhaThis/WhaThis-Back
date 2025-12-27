package com.example.whathis.follow.repository;

import com.example.whathis.follow.entity.Follow;
import com.example.whathis.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    // 팔로우 여부 확인 (중복 팔로우 방지)
    boolean existsByFollowerAndFollowing(User follower, User following);

    // 팔로우 취소를 위한 조회
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    // 팔로워수
    long countByFollowingId(Long followingId);

    // 팔로잉수
    long countByFollowerId(Long followerId);

    // 내가 팔로우한 사용자 목록 (Followings)
    @Query("SELECT f FROM Follow f JOIN FETCH f.following WHERE f.follower.id = :followerId")
    List<Follow> findFollowingsByFollowerId(@Param("followerId") Long followerId);

    // 나를 팔로우한 사용자 목록 (Followers)
    @Query("SELECT f FROM Follow f JOIN FETCH f.follower WHERE f.following.id = :followingId")
    List<Follow> findFollowersByFollowingId(@Param("followingId") Long followingId);
}
