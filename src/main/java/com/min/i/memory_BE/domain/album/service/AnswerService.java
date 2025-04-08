package com.min.i.memory_BE.domain.album.service;

import com.min.i.memory_BE.domain.album.entity.Answer;
import com.min.i.memory_BE.domain.album.repository.AnswerRepository;
import com.min.i.memory_BE.domain.album.dto.response.AnswerResponse;
import com.min.i.memory_BE.domain.album.entity.Question;
import com.min.i.memory_BE.domain.album.repository.QuestionRepository;
import com.min.i.memory_BE.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final SpeechToTextService speechToTextService;
    
    /**
     * 음성 또는 텍스트 답변을 저장합니다.
     */
    @Transactional
    public Answer saveAnswer(Long questionId, User user, String textContent, MultipartFile audioFile) {
        // 1. 질문 조회
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다: " + questionId));
        
        String content;
        
        // 2. 음성 파일이 제공된 경우, STT로 변환
        if (audioFile != null && !audioFile.isEmpty()) {
            log.info("음성 답변 처리 시작: 사용자 {}의 질문 {} 답변", user.getEmail(), questionId);
            content = speechToTextService.convertSpeechToText(audioFile);
        } else {
            // 3. 텍스트 답변인 경우 그대로 사용
            log.info("텍스트 답변 처리: 사용자 {}의 질문 {} 답변", user.getEmail(), questionId);
            content = textContent;
        }
        
        // 4. Answer 엔티티 생성 및 저장
        Answer answer = Answer.builder()
                .question(question)
                .user(user)
                .content(content)
                .build();
        
        return answerRepository.save(answer);
    }
    
    //SpeechToTextService 인스턴스를 반환합니다.
    public SpeechToTextService getSpeechToTextService() {
        return speechToTextService;
    }
    
    @Transactional(readOnly = true)
    public List<AnswerResponse> getAnswersByQuestionId(Long questionId) {
        return answerRepository.findByQuestionId(questionId).stream()
                .map(AnswerResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnswerResponse> getAnswersByUserId(Long userId) {
        return answerRepository.findByUserId(userId).stream()
                .map(AnswerResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AnswerResponse getAnswerById(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("답변을 찾을 수 없습니다: " + answerId));
        return AnswerResponse.from(answer);
    }
}