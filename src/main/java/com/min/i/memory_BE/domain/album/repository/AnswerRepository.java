package com.min.i.memory_BE.domain.album.repository;

import com.min.i.memory_BE.domain.album.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    // src/main/java/com/min/i/memory_BE/domain/album/repository/AnswerRepository.java에 추가
    List<Answer> findByQuestionId(Long questionId);
    List<Answer> findByUserId(Long userId);

}