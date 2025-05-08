package com.min.i.memory_BE.domain.album.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // 테스트 프로파일 활성화
public class SpeechToTextServiceTest {

    private static final Logger logger = Logger.getLogger(SpeechToTextServiceTest.class.getName());

    private String credentialsFilePath;
    
    @BeforeEach
    public void setUp() {
        // 테스트 실행 전 환경 변수 설정
        try {
            Path rootPath = Paths.get("").toAbsolutePath();
            File credentialsFile = new File(rootPath.toString(), "src/test/resources/keys/google-credentials.json");
            
            if (credentialsFile.exists()) {
                credentialsFilePath = credentialsFile.getAbsolutePath();
                logger.info("인증 파일 경로 직접 설정: " + credentialsFilePath);
                
                // 환경 변수 직접 설정 (System.setProperty 사용)
                System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", credentialsFilePath);
                logger.info("환경 변수 GOOGLE_APPLICATION_CREDENTIALS 설정 완료");
            } else {
                logger.warning("인증 파일을 찾을 수 없습니다: " + credentialsFile.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.severe("환경 변수 설정 중 오류: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("SpeechClient를 직접 사용한 음성 변환 테스트")
    public void testDirectSpeechClientUsage() throws Exception {
        try {
            // 1. 테스트할 음성 파일 경로 찾기
            Path rootPath = Paths.get("").toAbsolutePath();
            Path audioFilePath = Paths.get(rootPath.toString(), "mp4/테스트.flac");
            
            logger.info("테스트 파일 경로: " + audioFilePath);
            logger.info("파일 존재 여부: " + Files.exists(audioFilePath));
            assertTrue(Files.exists(audioFilePath), "테스트 음성 파일이 존재해야 합니다.");

            // 2. 파일 읽기
            byte[] audioBytes = Files.readAllBytes(audioFilePath);
            logger.info("오디오 파일 크기: " + audioBytes.length + " 바이트");
            
            // 3. 인증 파일 경로를 명시적으로 설정
            Path credentialsPath = Paths.get(rootPath.toString(), "src/test/resources/keys/google-credentials.json");
            logger.info("인증 파일 경로: " + credentialsPath);
            logger.info("인증 파일 존재 여부: " + Files.exists(credentialsPath));
            
            // 4. Speech API 직접 호출
            try (FileInputStream credentialsStream = new FileInputStream(credentialsPath.toFile())) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
                logger.info("Google 인증 정보 생성 성공");
                
                SpeechSettings settings = SpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();
                logger.info("Speech 설정 생성 성공");
                
                try (SpeechClient speechClient = SpeechClient.create(settings)) {
                    logger.info("SpeechClient 생성 성공");
                    
                    RecognitionConfig config = RecognitionConfig.newBuilder()
                        .setEncoding(RecognitionConfig.AudioEncoding.FLAC)
                        .setSampleRateHertz(44100)
                        .setLanguageCode("ko-KR")
                        .build();
                    
                    RecognitionAudio audio = RecognitionAudio.newBuilder()
                        .setContent(ByteString.copyFrom(audioBytes))
                        .build();
                    
                    logger.info("음성 인식 요청 시작");
                    RecognizeResponse response = speechClient.recognize(config, audio);
                    logger.info("음성 인식 응답 받음");
                    
                    StringBuilder transcription = new StringBuilder();
                    for (SpeechRecognitionResult result : response.getResultsList()) {
                        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                        transcription.append(alternative.getTranscript());
                    }
                    
                    String recognizedText = transcription.toString();
                    logger.info("변환 결과: " + recognizedText);
                    assertNotNull(recognizedText, "변환 결과는 null이 아니어야 합니다.");
                }
            }
        } catch (Exception e) {
            logger.severe("직접 호출 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 