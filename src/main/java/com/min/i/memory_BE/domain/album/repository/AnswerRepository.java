package com.min.i.memory_BE.domain.album.repository;

import com.min.i.memory_BE.domain.album.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    // 추가 메서드가 필요하면 여기에 정의
}