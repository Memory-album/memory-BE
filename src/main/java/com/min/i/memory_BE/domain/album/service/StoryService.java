package com.min.i.memory_BE.domain.album.service;

import com.min.i.memory_BE.domain.album.entity.Answer;
import com.min.i.memory_BE.domain.album.entity.Question;
import com.min.i.memory_BE.domain.album.entity.Story;
import com.min.i.memory_BE.domain.album.repository.AnswerRepository;
import com.min.i.memory_BE.domain.album.repository.QuestionRepository;
import com.min.i.memory_BE.domain.album.repository.StoryRepository;
import com.min.i.memory_BE.domain.media.client.FastApiClient;
import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.media.repository.MediaRepository;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import com.min.i.memory_BE.global.error.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StoryService {

    private final MediaRepository mediaRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final StoryRepository storyRepository;
    private final FastApiClient fastApiClient;
    
    /**
     * 미디어 ID로 스토리를 조회합니다.
     * 
     * @param mediaId 미디어 ID
     * @return 조회된 스토리 엔티티
     * @throws EntityNotFoundException 스토리가 존재하지 않을 경우
     */
    public Story getStoryByMediaId(Long mediaId) {
        log.info("미디어 ID {}에 대한 스토리 조회", mediaId);
        return storyRepository.findByMediaId(mediaId)
                .orElseThrow(() -> new EntityNotFoundException("스토리를 찾을 수 없습니다: 미디어 ID " + mediaId));
    }
    
    /**
     * 미디어 ID를 기반으로 질문과 답변을 조회하여 스토리를 생성합니다.
     * 생성된 스토리는 데이터베이스에 저장됩니다.
     * 
     * @param mediaId 미디어 ID
     * @return 생성된 스토리 엔티티
     * @throws DuplicateResourceException 이미 스토리가 존재할 경우
     */
    @Transactional
    public Story generateStory(Long mediaId) {
        log.info("미디어 ID {}에 대한 스토리 생성 시작", mediaId);
        
        // 1. 미디어 조회
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new EntityNotFoundException("미디어를 찾을 수 없습니다: " + mediaId));
        
        // 2. 이미 스토리가 있는지 확인
        if (storyRepository.existsByMediaId(mediaId)) {
            log.info("이미 스토리가 존재합니다. ID: {}", mediaId);
            throw new DuplicateResourceException("이미 해당 미디어에 대한 스토리가 존재합니다: " + mediaId);
        }
        
        // 3. 질문과 답변 데이터 조회
        List<Question> questions = questionRepository.findByMediaId(mediaId);
        List<Answer> answers = answerRepository.findByMediaId(mediaId);
        
        if (questions.isEmpty() || answers.isEmpty()) {
            log.warn("미디어 {}에 대한 질문 또는 답변이 없습니다", mediaId);
            throw new IllegalStateException("스토리 생성을 위한 질문과 답변이 충분하지 않습니다");
        }
        
        // 4. FastAPI 요청 데이터 구성
        List<Map<String, Object>> questionsList = questions.stream()
                .map(q -> {
                    Map<String, Object> qMap = new HashMap<>();
                    qMap.put("id", q.getId());
                    qMap.put("content", q.getContent());
                    qMap.put("category", q.getCategory());
                    qMap.put("level", q.getLevel());
                    qMap.put("theme", q.getTheme() != null ? q.getTheme().name() : null);
                    return qMap;
                })
                .collect(Collectors.toList());
        
        List<Map<String, Object>> answersList = answers.stream()
                .map(a -> {
                    Map<String, Object> aMap = new HashMap<>();
                    aMap.put("id", a.getId());
                    aMap.put("content", a.getContent());
                    aMap.put("user_id", a.getUser() != null ? a.getUser().getId() : null);
                    return aMap;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> options = new HashMap<>();
        options.put("style", "emotional");
        options.put("length", "medium");
        
        // 미디어의 이미지 URL 가져오기
        String imageUrl = media.getImageUrl(); // 이미지 URL 우선사용
        // 이미지 URL이 없으면 파일 URL 사용
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = media.getFileUrl();
        }
        
        log.info("스토리 생성에 사용할 이미지 URL: {}", imageUrl);
        
        // 5. FastAPI 호출하여 스토리 생성 - 이미지 URL 전달
        Map<String, Object> response = fastApiClient.generateStory(mediaId, questionsList, answersList, options, imageUrl);
        
        // 6. 응답 처리
        if (response == null || !"success".equals(response.get("status"))) {
            String errorMsg = response != null ? 
                response.get("message") != null ? response.get("message").toString() : "알 수 없는 오류" 
                : "응답이 null입니다";
            log.error("스토리 생성 실패: {}", errorMsg);
            throw new RuntimeException("스토리 생성 실패: " + errorMsg);
        }
        
        String storyContent = (String) response.get("story_content");
        
        // 7. 스토리 저장
        Story story = Story.builder()
                .media(media)
                .content(storyContent)
                .build();
        
        Story savedStory = storyRepository.save(story);
        log.info("스토리가 성공적으로 생성되었습니다. 스토리 ID: {}", savedStory.getId());
        
        return savedStory;
    }

} 