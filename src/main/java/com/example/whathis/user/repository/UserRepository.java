package com.example.whathis.user.repository;

import com.example.whathis.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // 중복 검사
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
}
