package com.min.i.memory_BE.domain.user.repository;

import com.min.i.memory_BE.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 유저 조회
    Optional<User> findByEmail(String email);

    // 이메일 중복 체크
    boolean existsByEmail(String email);

}
