package com.min.i.memory_BE.domain.album.service;

import com.min.i.memory_BE.domain.album.dto.QuestionDto;
import com.min.i.memory_BE.domain.album.entity.Question;
import com.min.i.memory_BE.domain.album.enums.QuestionTheme;
import com.min.i.memory_BE.domain.album.repository.QuestionRepository;
import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.media.repository.MediaRepository;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final MediaRepository mediaRepository;

    /**
     * 사용자가 입력한 질문을 생성합니다.
     * 미디어 ID와 질문 내용만 입력받고 나머지 값은 기본값으로 설정합니다.
     *
     * @param requestDto 질문 생성 요청 DTO
     * @return 생성된 질문의 응답 DTO
     */
    @Transactional
    public QuestionDto.Response createUserQuestion(QuestionDto.Create requestDto) {
        log.info("사용자 입력 질문 생성 시작: mediaId={}, content={}", requestDto.getMediaId(), requestDto.getContent());

        // 1. 미디어 존재 확인
        Media media = mediaRepository.findById(requestDto.getMediaId())
                .orElseThrow(() -> new EntityNotFoundException("미디어를 찾을 수 없습니다: " + requestDto.getMediaId()));

        // 2. 기본값 설정
        // 테마는 미디어가 속한 앨범의 테마를 참고하여 설정할 수 있음
        QuestionTheme theme = determineTheme(media);
        
        // 카테고리와 레벨 기본값 설정
        String category = "USER_QUESTION";
        Integer level = 1; // 기본 난이도
        
        // 3. 질문 엔티티 생성 및 저장
        Question question = Question.builder()
                .media(media)
                .content(requestDto.getContent())
                .theme(theme)
                .isPrivate(false) // 기본적으로 공개 질문
                .keywordsUsed("") // 사용자 입력 질문은 키워드 없음
                .level(level)
                .category(category)
                .build();
        
        Question savedQuestion = questionRepository.save(question);
        log.info("사용자 입력 질문 저장 완료: questionId={}", savedQuestion.getId());
        
        return QuestionDto.Response.fromEntity(savedQuestion);
    }
    
    /**
     * 질문을 수정합니다.
     *
     * @param questionId 수정할 질문 ID
     * @param requestDto 질문 수정 요청 DTO
     * @return 수정된 질문의 응답 DTO
     */
    @Transactional
    public QuestionDto.Response updateQuestion(Long questionId, QuestionDto.Update requestDto) {
        log.info("질문 수정 시작: questionId={}, content={}", questionId, requestDto.getContent());

        // 1. 질문 존재 확인
        Question question = questionRepository.findByIdWithMediaAndUploader(questionId)
                .orElseThrow(() -> new EntityNotFoundException("질문을 찾을 수 없습니다: " + questionId));

        // 2. 질문 내용 수정
        question.setContent(requestDto.getContent());
        
        // 3. 수정된 질문 저장
        Question updatedQuestion = questionRepository.save(question);
        log.info("질문 수정 완료: questionId={}", updatedQuestion.getId());
        
        return QuestionDto.Response.fromEntity(updatedQuestion);
    }

    /**
     * 미디어 ID로 질문 목록을 조회합니다.
     *
     * @param mediaId 미디어 ID
     * @return 질문 목록
     */
    public List<QuestionDto.Response> getQuestionsByMediaId(Long mediaId) {
        log.info("미디어 ID로 질문 목록 조회: mediaId={}", mediaId);
        
        // 미디어 존재 확인
        if (!mediaRepository.existsById(mediaId)) {
            throw new EntityNotFoundException("미디어를 찾을 수 없습니다: " + mediaId);
        }
        
        List<Question> questions = questionRepository.findByMediaIdWithMediaAndUploader(mediaId);
        return questions.stream()
                .map(QuestionDto.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 질문 ID로 질문 상세 정보를 조회합니다.
     *
     * @param questionId 질문 ID
     * @return 질문 상세 정보
     */
    public QuestionDto.Response getQuestionById(Long questionId) {
        log.info("질문 상세 조회: questionId={}", questionId);
        
        Question question = questionRepository.findByIdWithMediaAndUploader(questionId)
                .orElseThrow(() -> new EntityNotFoundException("질문을 찾을 수 없습니다: " + questionId));
        
        return QuestionDto.Response.fromEntity(question);
    }
    
    /**
     * 미디어 정보를 기반으로 적절한 질문 테마를 결정합니다.
     * 미디어가 속한 앨범의 테마를 참고할 수 있습니다.
     *
     * @param media 미디어 엔티티
     * @return 결정된 질문 테마
     */
    private QuestionTheme determineTheme(Media media) {
        // 미디어가 속한 앨범이 있고, 앨범의 테마가 있는 경우 관련된 테마 반환
        if (media.getAlbum() != null && media.getAlbum().getTheme() != null) {
            String albumThemeName = media.getAlbum().getTheme().name();
            
            // 앨범 테마와 일치하는 질문 테마 매핑
            if (albumThemeName.contains("SENIOR")) {
                return QuestionTheme.SENIOR_CARE;
            } else if (albumThemeName.contains("CHILD")) {
                return QuestionTheme.CHILD_STORY;
            } else if (albumThemeName.contains("COUPLE")) {
                return QuestionTheme.COUPLE_STORY;
            }
        }
        
        // 기본 테마
        return QuestionTheme.SENIOR_CARE;
    }

    /**
     * 미디어 ID로 미답변 질문 목록을 조회합니다.
     *
     * @param mediaId 미디어 ID
     * @return 미답변 질문 목록
     */
    public List<QuestionDto.Response> getUnansweredQuestionsByMediaId(Long mediaId) {
        log.info("미디어 ID로 미답변 질문 목록 조회: mediaId={}", mediaId);
        
        // 미디어 존재 확인
        if (!mediaRepository.existsById(mediaId)) {
            throw new EntityNotFoundException("미디어를 찾을 수 없습니다: " + mediaId);
        }
        
        List<Question> questions = questionRepository.findUnansweredQuestionsByMediaId(mediaId);
        return questions.stream()
                .map(QuestionDto.Response::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 미디어 ID로 답변된 질문 목록을 조회합니다.
     *
     * @param mediaId 미디어 ID
     * @return 답변된 질문 목록
     */
    public List<QuestionDto.Response> getAnsweredQuestionsByMediaId(Long mediaId) {
        log.info("미디어 ID로 답변된 질문 목록 조회: mediaId={}", mediaId);
        
        // 미디어 존재 확인
        if (!mediaRepository.existsById(mediaId)) {
            throw new EntityNotFoundException("미디어를 찾을 수 없습니다: " + mediaId);
        }
        
        List<Question> questions = questionRepository.findAnsweredQuestionsByMediaId(mediaId);
        return questions.stream()
                .map(QuestionDto.Response::fromEntity)
                .collect(Collectors.toList());
    }
} 