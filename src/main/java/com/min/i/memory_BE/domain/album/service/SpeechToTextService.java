package com.min.i.memory_BE.domain.album.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.protobuf.ByteString;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpeechToTextService {

    @Value("${google.speech.language:ko-KR}")
    private String language;
    
    @Value("${google.speech.credentials-file:classpath:keys/google-credentials.json}")
    private String credentialsFilePath;
    
    private final ResourceLoader resourceLoader;
    
    // 객체 초기화 시 환경 변수 설정
    @PostConstruct
    public void init() {
        try {
            // 인증 파일 경로 확인
            Resource credentialsResource = resourceLoader.getResource(credentialsFilePath);
            if (credentialsResource.exists()) {
                File credentialsFile = credentialsResource.getFile();
                log.info("인증 파일 경로: {}", credentialsFile.getAbsolutePath());
                
                // 환경 변수 직접 설정 (테스트와 동일한 방식)
                System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", credentialsFile.getAbsolutePath());
                log.info("GOOGLE_APPLICATION_CREDENTIALS 환경 변수 설정 완료");
            } else {
                log.error("인증 파일을 찾을 수 없습니다: {}", credentialsFilePath);
            }
        } catch (Exception e) {
            log.error("인증 파일 설정 중 오류 발생", e);
        }
    }

    /**
     * 음성 파일을 텍스트로 변환합니다.
     */
    public String convertSpeechToText(MultipartFile audioFile) {
        Path tempFile = null;
        
        try {
            log.info("음성 파일 변환 시작: {}, 타입: {}", audioFile.getOriginalFilename(), audioFile.getContentType());
            
            // 1. 음성 파일을 임시 파일로 저장
            String extension = getFileExtension(audioFile.getOriginalFilename());
            tempFile = Files.createTempFile("speech-", "." + extension);
            audioFile.transferTo(tempFile.toFile());
            
            // 2. 파일을 바이트 배열로 읽기
            byte[] audioBytes = Files.readAllBytes(tempFile);
            log.info("오디오 파일 크기: {} 바이트", audioBytes.length);
            
            // 3. 인증 파일 경로 확인
            Resource credentialsResource = resourceLoader.getResource(credentialsFilePath);
            
            if (!credentialsResource.exists()) {
                log.error("인증 파일을 찾을 수 없습니다: {}", credentialsFilePath);
                throw new IOException("Google API 인증 파일을 찾을 수 없습니다: " + credentialsFilePath);
            }
            
            // 테스트와 유사한 방식으로 SpeechClient 생성
            try (FileInputStream credentialsStream = new FileInputStream(credentialsResource.getFile())) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
                SpeechSettings settings = SpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();
                
                try (SpeechClient speechClient = SpeechClient.create(settings)) {
                    // 4. 오디오 데이터 생성
                    ByteString audioData = ByteString.copyFrom(audioBytes);
                    RecognitionAudio audio = RecognitionAudio.newBuilder()
                        .setContent(audioData)
                        .build();
                    
                    // 5. 인식 설정 구성 - 파일 타입에 따라 적절한 인코딩 설정
                    RecognitionConfig.AudioEncoding encoding = determineAudioEncoding(audioFile.getContentType(), extension);
                    log.info("오디오 인코딩 설정: {}", encoding);
                    
                    RecognitionConfig config = RecognitionConfig.newBuilder()
                        .setEncoding(encoding)
                        .setSampleRateHertz(44100)  // 테스트와 동일하게 44100 설정
                        .setLanguageCode(language)
                        .build();
                    
                    // 6. 음성 인식 요청
                    log.info("음성 인식 요청 시작");
                    RecognizeResponse response = speechClient.recognize(config, audio);
                    log.info("음성 인식 응답 받음");
                    
                    List<SpeechRecognitionResult> results = response.getResultsList();
                    
                    // 7. 결과 처리
                    if (results.isEmpty()) {
                        log.warn("인식된 텍스트가 없습니다.");
                        return "";
                    }
                    
                    StringBuilder transcription = new StringBuilder();
                    for (SpeechRecognitionResult result : results) {
                        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                        log.info("인식 결과: {}, 신뢰도: {}", alternative.getTranscript(), alternative.getConfidence());
                        transcription.append(alternative.getTranscript());
                    }
                    
                    String recognizedText = transcription.toString();
                    log.info("음성 인식 성공: {}", recognizedText);
                    return recognizedText;
                }
            }
            
        } catch (IOException e) {
            log.error("음성 파일 처리 중 오류 발생", e);
            throw new RuntimeException("음성 파일 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        } finally {
            // 임시 파일 삭제
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("임시 파일 삭제 실패", e);
                }
            }
        }
    }
    
    /**
     * 파일 확장자에 따라 적절한 오디오 인코딩을 결정합니다.
     */
    private RecognitionConfig.AudioEncoding determineAudioEncoding(String contentType, String extension) {
        if (contentType != null) {
            if (contentType.contains("flac")) {
                return RecognitionConfig.AudioEncoding.FLAC;
            } else if (contentType.contains("mp3")) {
                return RecognitionConfig.AudioEncoding.MP3;
            } else if (contentType.contains("wav") || contentType.contains("wave")) {
                return RecognitionConfig.AudioEncoding.LINEAR16;
            } else if (contentType.contains("ogg") || contentType.contains("opus")) {
                return RecognitionConfig.AudioEncoding.OGG_OPUS;
            }
        }
        
        // 확장자로 판단
        if ("flac".equalsIgnoreCase(extension)) {
            return RecognitionConfig.AudioEncoding.FLAC;
        } else if ("mp3".equalsIgnoreCase(extension)) {
            return RecognitionConfig.AudioEncoding.MP3;
        } else if ("wav".equalsIgnoreCase(extension) || "wave".equalsIgnoreCase(extension)) {
            return RecognitionConfig.AudioEncoding.LINEAR16;
        } else if ("ogg".equalsIgnoreCase(extension) || "opus".equalsIgnoreCase(extension)) {
            return RecognitionConfig.AudioEncoding.OGG_OPUS;
        } else if ("m4a".equalsIgnoreCase(extension)) {
            // m4a는 일반적으로 AAC 인코딩이지만, Google Speech API는 직접 지원하지 않음
            // 따라서 명시적인 인코딩을 지정하지 않음
            return RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED;
        }
        
        log.warn("알 수 없는 오디오 형식: {}({}), 인코딩 미지정", extension, contentType);
        return RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED;
    }
    
    /**
     * 파일명에서 확장자를 추출합니다.
     */
    private String getFileExtension(String filename) {
        if (filename == null) return "tmp";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "tmp";
        }
        return filename.substring(lastDotIndex + 1);
    }
}