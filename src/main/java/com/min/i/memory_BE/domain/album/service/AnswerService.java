package com.min.i.memory_BE.domain.album.service;

import com.min.i.memory_BE.domain.album.entity.Answer;
import com.min.i.memory_BE.domain.album.repository.AnswerRepository;
import com.min.i.memory_BE.domain.album.dto.response.AnswerResponse;
import com.min.i.memory_BE.domain.album.entity.Question;
import com.min.i.memory_BE.domain.album.repository.QuestionRepository;
import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.media.repository.MediaRepository;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
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
    private final MediaRepository mediaRepository;
    private final SpeechToTextService speechToTextService;
    
    /**
     * 미디어에 대한 음성 또는 텍스트 답변을 저장합니다.
     * 하나의 미디어에 연결된 모든 질문에 대한 답변으로 처리됩니다.
     */
    @Transactional
    public Answer saveAnswer(Long mediaId, User user, String textContent, MultipartFile audioFile) {
        // 1. 미디어 조회
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new EntityNotFoundException("미디어를 찾을 수 없습니다: " + mediaId));
        
        String content;
        
        // 2. 음성 파일이 제공된 경우, STT로 변환
        if (audioFile != null && !audioFile.isEmpty()) {
            log.info("음성 답변 처리 시작: 사용자 {}의 미디어 {} 답변", user.getEmail(), mediaId);
            content = speechToTextService.convertSpeechToText(audioFile);
        } else {
            // 3. 텍스트 답변인 경우 그대로 사용
            log.info("텍스트 답변 처리: 사용자 {}의 미디어 {} 답변", user.getEmail(), mediaId);
            content = textContent;
        }
        
        // 미디어와 연결된 질문을 가져옵니다
        List<Question> questions = questionRepository.findByMediaIdWithMediaAndUploader(mediaId);
        if (questions.isEmpty()) {
            throw new EntityNotFoundException("미디어에 연결된 질문이 없습니다: " + mediaId);
        }
        
        // 첫 번째 질문을 가져와 사용합니다
        Question question = questions.get(0);
        
        // 4. Answer 엔티티 생성 및 저장
        Answer answer = Answer.builder()
                .media(media)
                .user(user)
                .content(content)
                .question(question)
                .build();
        
        return answerRepository.save(answer);
    }
    
    /**
     * 미디어에 대한 음성 또는 텍스트 답변을 저장합니다 (질문 ID 지정).
     */
    @Transactional
    public Answer saveAnswer(Long mediaId, Long questionId, User user, String textContent, MultipartFile audioFile) {
        // 1. 미디어 조회
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new EntityNotFoundException("미디어를 찾을 수 없습니다: " + mediaId));
        
        // 2. 질문 조회
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("질문을 찾을 수 없습니다: " + questionId));
        
        // 3. 미디어와 질문의 관계 검증
        if (!question.getMedia().getId().equals(mediaId)) {
            throw new IllegalArgumentException("해당 질문이 미디어와 연결되어 있지 않습니다. 질문 ID: " + questionId + ", 미디어 ID: " + mediaId);
        }
        
        String content;
        
        // 4. 음성 파일이 제공된 경우, STT로 변환
        if (audioFile != null && !audioFile.isEmpty()) {
            log.info("음성 답변 처리 시작: 사용자 {}의 미디어 {} 답변", user.getEmail(), mediaId);
            content = speechToTextService.convertSpeechToText(audioFile);
        } else {
            // 5. 텍스트 답변인 경우 그대로 사용
            log.info("텍스트 답변 처리: 사용자 {}의 미디어 {} 답변", user.getEmail(), mediaId);
            content = textContent;
        }
        
        // 6. Answer 엔티티 생성 및 저장
        Answer answer = Answer.builder()
                .media(media)
                .user(user)
                .content(content)
                .question(question)
                .build();
        
        return answerRepository.save(answer);
    }
    
    /**
     * 미디어에 연결된 모든 질문을 조회합니다.
     */
    public List<Question> getQuestionsByMediaId(Long mediaId) {
        return questionRepository.findByMediaIdWithMediaAndUploader(mediaId);
    }
    
    /**
     * 특정 미디어에 대한 사용자의 답변을 조회합니다.
     */
    public List<Answer> getAnswersByMediaIdAndUserId(Long mediaId, Long userId) {
        return answerRepository.findByMediaIdAndUserId(mediaId, userId);
    }
    
    /**
     * 특정 미디어에 대한 가장 최근 답변을 조회합니다.
     */
    public Answer getLatestAnswerByMediaId(Long mediaId) {
        return answerRepository.findLatestByMediaId(mediaId)
                .orElse(null);
    }
    
    /**
     * SpeechToTextService 인스턴스를 반환합니다.
     */
    public SpeechToTextService getSpeechToTextService() {
        return speechToTextService;
    }

    /**
     * 질문 ID로 답변 목록을 조회합니다.
     */
    public List<AnswerResponse> getAnswersByQuestionId(Long questionId) {
        return answerRepository.findByQuestionId(questionId).stream()
                .map(AnswerResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 ID로 답변 목록을 조회합니다.
     */
    public List<AnswerResponse> getAnswersByUserId(Long userId) {
        return answerRepository.findByUserId(userId).stream()
                .map(AnswerResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 답변 ID로 답변을 조회합니다.
     */
    public AnswerResponse getAnswerById(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("답변을 찾을 수 없습니다: " + answerId));
        return AnswerResponse.from(answer);
    }
}