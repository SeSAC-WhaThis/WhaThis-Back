package com.example.whathis.follow.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FollowListResponse {

    private long count;
    private List<FollowUserResponse> users;

    public static FollowListResponse of(List<FollowUserResponse> users) {
        return FollowListResponse.builder()
                .count(users.size())
                .users(users)
                .build();
    }
}
