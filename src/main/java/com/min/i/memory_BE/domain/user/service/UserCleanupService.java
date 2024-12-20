package com.min.i.memory_BE.domain.user.service;

import com.min.i.memory_BE.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserCleanupService {

    private final UserRepository userRepository;

    @Autowired
    public UserCleanupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 매일 자정마다 인증 기한이 만료된 사용자 삭제
    @Scheduled(cron = "0 0 0 * * ?")  // 매일 자정에 실행
    public void cleanupExpiredUsers() {
        LocalDateTime now = LocalDateTime.now();
        // 인증 기한이 지난 사용자 삭제
        userRepository.deleteExpiredUsers(now);
    }
}