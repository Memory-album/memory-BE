package com.min.i.memory_BE.domain.media.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    /**
     * AI 분석 결과를 처리합니다.
     * @param mediaId 미디어 ID
     * @param analysisResult AI 분석 결과 JSON
     */
    @Transactional
    public void processAnalysisResult(Long mediaId, String analysisResult) {
        try {
            // 1. JSON 파싱
            Map<String, Object> analysisMap = objectMapper.readValue(analysisResult, Map.class);
            
            // 2. 미디어 엔티티 조회
            Media media = mediaRepository.findById(mediaId)
                    .orElseThrow(() -> new EntityNotFoundException("미디어를 찾을 수 없습니다."));
            
            // 3. 분석 결과 메타데이터 저장
            media.setAnalysisResult(analysisResult);
            
            // 4. 분석 결과에서 키워드 추출 및 저장
            processKeywords(media, analysisMap);
            
            // 5. 분석 결과에서 질문 추출 및 저장
            processQuestions(media, analysisMap);
            
        } catch (Exception e) {
            log.error("AI 분석 결과 처리 중 오류 발생", e);
            throw new RuntimeException("AI 분석 결과 처리 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 분석 결과에서 키워드를 추출하여 저장합니다.
     */
    private void processKeywords(Media media, Map<String, Object> analysisMap) {
        if (analysisMap.containsKey("analysis_result")) {
            Map<String, Object> result = (Map<String, Object>) analysisMap.get("analysis_result");
            
            // labels 처리
            if (result.containsKey("labels")) {
                List<Map<String, Object>> labels = (List<Map<String, Object>>) result.get("labels");
                for (Map<String, Object> label : labels) {
                    String description = (String) label.get("description");
                    float score = ((Number) label.get("score")).floatValue();
                    
                    // 키워드 저장 또는 조회
                    Keyword keyword = findOrCreateKeyword(description, KeywordCategory.OBJECT);
                    
                    // MediaKeyword 저장
                    createMediaKeyword(media, keyword, score);
                }
            }
            
            // objects 처리
            if (result.containsKey("objects")) {
                List<Map<String, Object>> objects = (List<Map<String, Object>>) result.get("objects");
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
     * 분석 결과에서 질문을 추출하여 저장합니다.
     */
    private void processQuestions(Media media, Map<String, Object> analysisMap) {
        if (analysisMap.containsKey("questions")) {
            List<Map<String, Object>> questions = (List<Map<String, Object>>) analysisMap.get("questions");
            List<String> keywordsUsed = new ArrayList<>();
            
            // 사용된 키워드 추출
            if (analysisMap.containsKey("analysis_result") && 
                ((Map<String, Object>) analysisMap.get("analysis_result")).containsKey("labels")) {
                
                List<Map<String, Object>> labels = (List<Map<String, Object>>) 
                    ((Map<String, Object>) analysisMap.get("analysis_result")).get("labels");
                
                for (Map<String, Object> label : labels) {
                    keywordsUsed.add((String) label.get("description"));
                    if (keywordsUsed.size() >= 5) break; // 최대 5개 키워드만 사용
                }
            }
            
            String keywordsStr = String.join(",", keywordsUsed);
            
            for (Map<String, Object> questionMap : questions) {
                String content = (String) questionMap.get("question");
                String category = (String) questionMap.get("category");
                
                // 질문 테마 결정
                QuestionTheme theme = mapCategoryToTheme(category);
                
                // 질문 저장
                Question question = Question.builder()
                        .media(media)
                        .content(content)
                        .theme(theme)
                        .isPrivate(false)
                        .keywordsUsed(keywordsStr)
                        .build();
                
                questionRepository.save(question);
            }
        }
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
} 