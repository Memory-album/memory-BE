package com.min.i.memory_BE.domain.user.repository;

import com.min.i.memory_BE.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 유저 조회
    Optional<User> findByEmail(String email);

    @Modifying
    @Query("DELETE FROM User u WHERE u.emailVerificationExpiredAt < :now AND u.mailStatus = 'UNVERIFIED'")
    void deleteExpiredUsers(LocalDateTime now);

}
