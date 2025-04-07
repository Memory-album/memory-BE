package com.min.i.memory_BE.domain.media.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.min.i.memory_BE.domain.album.entity.Album;
import com.min.i.memory_BE.domain.album.entity.Question;
import com.min.i.memory_BE.domain.album.enums.QuestionTheme;
import com.min.i.memory_BE.domain.album.repository.QuestionRepository;
import com.min.i.memory_BE.domain.media.entity.Keyword;
import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.media.entity.MediaKeyword;
import com.min.i.memory_BE.domain.media.enums.KeywordCategory;
import com.min.i.memory_BE.domain.media.repository.KeywordRepository;
import com.min.i.memory_BE.domain.media.repository.MediaKeywordRepository;
import com.min.i.memory_BE.domain.media.repository.MediaRepository;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import com.min.i.memory_BE.domain.media.client.FastApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaAnalysisService {

    private final MediaRepository mediaRepository;
    private final KeywordRepository keywordRepository;
    private final MediaKeywordRepository mediaKeywordRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;
    private final FastApiClient fastApiClient;

    /**
     * 빈 미디어 엔티티 생성
     */
    @Transactional
    public Media createEmptyMedia() {
        Media media = Media.builder()
                .fileSize(0L)
                .fileUrl("")
                .fileType(com.min.i.memory_BE.domain.media.enums.MediaType.IMAGE)
                .originalFilename("empty")
                .build();
        return mediaRepository.save(media);
    }
    
    /**
     * 사용자와 앨범 정보를 포함한 미디어 엔티티 생성
     */
    @Transactional
    public Media createEmptyMedia(User user, Album album) {
        Media media = Media.builder()
                .uploadedBy(user)
                .album(album)
                .fileSize(0L)
                .fileUrl("")
                .fileType(com.min.i.memory_BE.domain.media.enums.MediaType.IMAGE)
                .originalFilename("empty")
                .build();
        return mediaRepository.save(media);
    }
    
    /**
     * 미디어 엔티티 업데이트
     */
    @Transactional
    public Media updateMedia(Media media) {
        return mediaRepository.save(media);
    }
    
    /**
     * FastAPI로부터 받은 분석 결과 처리
     * @return 생성된 질문 리스트와 함께 업데이트된 분석 데이터
     */
    @Transactional
    public Map<String, Object> processAnalysisResult(Long mediaId, Map<String, Object> analysisData) {
        try {
            // 1. 미디어 엔티티 조회
            Media media = mediaRepository.findById(mediaId)
                    .orElseThrow(() -> new EntityNotFoundException("미디어를 찾을 수 없습니다: " + mediaId));
            
            // 2. 전체 분석 결과 저장
            String analysisJson = objectMapper.writeValueAsString(analysisData);
            media.setAnalysisResult(analysisJson);
            
            // 3. 분석 결과에서 키워드 추출 및 저장
            if (analysisData.containsKey("analysis_result")) {
                Map<String, Object> analysisResult = (Map<String, Object>) analysisData.get("analysis_result");
                processKeywordsFromAnalysisResult(media, analysisResult);
            }
            
            // 4. 질문 처리 및 ID 포함된 리스트 받기
            List<Map<String, Object>> savedQuestions = new ArrayList<>();
            if (analysisData.containsKey("questions")) {
                List<Map<String, Object>> questions = (List<Map<String, Object>>) analysisData.get("questions");
                savedQuestions = processQuestionsFromFastAPI(media, questions, analysisData);
                // 분석 데이터의 questions를 ID가 포함된 버전으로 업데이트
                analysisData.put("questions", savedQuestions);
            }
            
            // 5. 미디어 엔티티 저장
            mediaRepository.save(media);
            
            log.info("분석 결과 처리 완료: mediaId={}", mediaId);
            
            return analysisData;
            
        } catch (Exception e) {
            log.error("분석 결과 처리 중 오류 발생", e);
            throw new RuntimeException("분석 결과 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * FastAPI 분석 결과에서 키워드를 추출하여 저장합니다.
     */
    private void processKeywordsFromAnalysisResult(Media media, Map<String, Object> analysisResult) {
        // labels 처리
        if (analysisResult.containsKey("labels")) {
            List<Map<String, Object>> labels = (List<Map<String, Object>>) analysisResult.get("labels");
            for (Map<String, Object> label : labels) {
                String description = (String) label.get("description");
                float score = ((Number) label.get("score")).floatValue();
                
                // 키워드 저장 또는 조회
                Keyword keyword = findOrCreateKeyword(description, determineCategory(description));
                
                // MediaKeyword 저장
                createMediaKeyword(media, keyword, score);
            }
        }
        
        // objects 처리
        if (analysisResult.containsKey("objects")) {
            List<Map<String, Object>> objects = (List<Map<String, Object>>) analysisResult.get("objects");
            for (Map<String, Object> object : objects) {
                String name = (String) object.get("name");
                float score = ((Number) object.get("score")).floatValue();
                
                // 키워드 저장 또는 조회
                Keyword keyword = findOrCreateKeyword(name, KeywordCategory.OBJECT);
                
                // MediaKeyword 저장
                createMediaKeyword(media, keyword, score);
            }
        }
    }
    
    /**
     * 키워드를 찾거나 새로 생성합니다.
     */
    private Keyword findOrCreateKeyword(String name, KeywordCategory category) {
        return keywordRepository.findByName(name)
                .orElseGet(() -> {
                    Keyword newKeyword = Keyword.builder()
                            .name(name)
                            .category(category)
                            .build();
                    return keywordRepository.save(newKeyword);
                });
    }
    
    /**
     * MediaKeyword를 생성합니다.
     */
    private void createMediaKeyword(Media media, Keyword keyword, float confidenceScore) {
        // 이미 존재하는지 확인
        Optional<MediaKeyword> existingMediaKeyword = mediaKeywordRepository
                .findByMediaAndKeyword(media, keyword);
        
        if (existingMediaKeyword.isEmpty()) {
            MediaKeyword mediaKeyword = MediaKeyword.builder()
                    .media(media)
                    .keyword(keyword)
                    .confidenceScore(confidenceScore)
                    .build();
            
            mediaKeywordRepository.save(mediaKeyword);
            media.addMediaKeyword(mediaKeyword);
        }
    }
    
    /**
     * 키워드 카테고리를 결정합니다.
     */
    private KeywordCategory determineCategory(String keyword) {
        // 간단한 카테고리 결정 로직 (실제로는 더 복잡한 로직이 필요할 수 있음)
        keyword = keyword.toLowerCase();
        
        if (keyword.contains("person") || keyword.contains("people") || keyword.contains("face")) {
            return KeywordCategory.OBJECT;
        } else if (keyword.contains("happy") || keyword.contains("sad") || keyword.contains("joy")) {
            return KeywordCategory.EMOTION;
        } else if (keyword.contains("walk") || keyword.contains("run") || keyword.contains("play")) {
            return KeywordCategory.ACTION;
        } else if (keyword.contains("park") || keyword.contains("house") || keyword.contains("school")) {
            return KeywordCategory.PLACE;
        } else if (keyword.contains("birthday") || keyword.contains("wedding") || keyword.contains("party")) {
            return KeywordCategory.EVENT;
        }
        
        return KeywordCategory.OBJECT; // 기본값
    }
    
    /**
     * FastAPI에서 받은 질문을 처리합니다.
     * @return 생성된 질문과 ID 리스트
     */
    private List<Map<String, Object>> processQuestionsFromFastAPI(Media media, List<Map<String, Object>> questions, Map<String, Object> analysisData) {
        // 키워드 추출
        List<String> keywordsUsed = extractKeywordsFromAnalysisResult(analysisData);
        String keywordsStr = String.join(",", keywordsUsed);
        
        // 생성된 질문 저장
        List<Map<String, Object>> savedQuestions = new ArrayList<>();
        
        for (Map<String, Object> questionMap : questions) {
            String content = (String) questionMap.get("question");
            String category = (String) questionMap.get("category");
            Integer level = (Integer) questionMap.get("level");
            
            // 질문 테마 결정
            QuestionTheme theme = mapCategoryToTheme(category);
            
            // 질문 저장
            Question question = Question.builder()
                    .media(media)
                    .content(content)
                    .theme(theme)
                    .isPrivate(false)
                    .keywordsUsed(keywordsStr)
                    .level(level)
                    .category(category)
                    .build();
            
            Question savedQuestion = questionRepository.save(question);
            log.info("질문 저장 완료: {}, ID: {}", content, savedQuestion.getId());
            
            // 원본 질문 정보에 ID 추가
            Map<String, Object> savedQuestionMap = new HashMap<>(questionMap);
            savedQuestionMap.put("id", savedQuestion.getId());
            savedQuestions.add(savedQuestionMap);
        }
        
        return savedQuestions;
    }
    
    /**
     * 분석 결과에서 키워드를 추출합니다.
     */
    private List<String> extractKeywordsFromAnalysisResult(Map<String, Object> analysisData) {
        List<String> keywordsUsed = new ArrayList<>();
        
        if (analysisData.containsKey("analysis_result")) {
            Map<String, Object> analysisResult = (Map<String, Object>) analysisData.get("analysis_result");
            
            if (analysisResult.containsKey("labels")) {
                List<Map<String, Object>> labels = (List<Map<String, Object>>) analysisResult.get("labels");
                
                for (Map<String, Object> label : labels) {
                    keywordsUsed.add((String) label.get("description"));
                    if (keywordsUsed.size() >= 5) break; // 최대 5개 키워드만 사용
                }
            }
        }
        
        return keywordsUsed;
    }
    
    /**
     * 질문 카테고리를 테마로 매핑합니다.
     */
    private QuestionTheme mapCategoryToTheme(String category) {
        switch (category.toLowerCase()) {
            case "temporal":
            case "sensory":
                return QuestionTheme.SENIOR_CARE;
            case "relational":
                return QuestionTheme.COUPLE_STORY;
            default:
                return QuestionTheme.SENIOR_CARE;
        }
    }

    /**
     * 이미지를 분석하고 결과를 저장합니다.
     */
    @Transactional
    public void analyzeAndSaveImage(Long mediaId, MultipartFile image) {
        try {
            // 1. FastAPI 서버에 이미지 전송 및 분석 요청
            Map<String, Object> analysisResult = fastApiClient.analyzeImage(image);
            
            // 2. 분석 결과 처리
            processAnalysisResult(mediaId, analysisResult);
            
        } catch (Exception e) {
            log.error("이미지 분석 중 오류 발생: mediaId={}", mediaId, e);
            throw new RuntimeException("이미지 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
} 