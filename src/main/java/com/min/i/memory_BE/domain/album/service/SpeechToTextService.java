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
    private final AudioFormatConverter audioFormatConverter;
    
    // FFmpeg 사용 가능 여부
    private boolean ffmpegAvailable = false;
    
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
            
            // FFmpeg 사용 가능 여부 확인
            ffmpegAvailable = audioFormatConverter.isFFmpegAvailable();
            if (ffmpegAvailable) {
                log.info("FFmpeg 설치 확인 완료, 오디오 형식 변환 기능 활성화");
            } else {
                log.warn("FFmpeg가 설치되어 있지 않아 오디오 형식 변환 기능을 사용할 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("초기화 중 오류 발생", e);
        }
    }

    /**
     * 음성 파일을 텍스트로 변환합니다.
     */
    public String convertSpeechToText(MultipartFile audioFile) {
        Path tempFile = null;
        Path convertedFile = null;
        boolean isConvertedFile = false;
        
        try {
            log.info("음성 파일 변환 시작: {}, 타입: {}", audioFile.getOriginalFilename(), audioFile.getContentType());
            
            // 파일 확장자 확인
            String extension = getFileExtension(audioFile.getOriginalFilename());
            
            // 1. FFmpeg가 설치되어 있고, 지원되지 않는 형식이면 변환 시도
            if (ffmpegAvailable) {
                log.info("오디오 형식 변환 기능 사용 가능, 필요 시 변환 시도");
                convertedFile = audioFormatConverter.convertToSupportedFormat(audioFile);
                
                if (convertedFile != null) {
                    tempFile = convertedFile;
                    isConvertedFile = true;
                    extension = "flac"; // 변환된 파일은 항상 FLAC
                    log.info("오디오 파일 변환 성공: {} -> FLAC", getFileExtension(audioFile.getOriginalFilename()));
                } else {
                    log.warn("오디오 파일 변환 실패, 원본 파일 사용");
                }
            }
            
            // 변환 실패 또는 FFmpeg가 없는 경우 기존 로직 사용
            if (tempFile == null) {
                tempFile = Files.createTempFile("speech-", "." + extension);
                audioFile.transferTo(tempFile.toFile());
            }
            
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
                    RecognitionConfig.AudioEncoding encoding;
                    
                    // 변환된 파일인 경우 FLAC 인코딩 사용
                    if (isConvertedFile) {
                        encoding = RecognitionConfig.AudioEncoding.FLAC;
                        log.info("변환된 FLAC 파일 사용, FLAC 인코딩 설정");
                    } else {
                        encoding = determineAudioEncoding(audioFile.getContentType(), extension);
                    }
                    
                    log.info("오디오 인코딩 설정: {}", encoding);
                    
                    // 6. RecognitionConfig 구성
                    RecognitionConfig.Builder configBuilder = RecognitionConfig.newBuilder()
                        .setEncoding(encoding)
                        .setLanguageCode(language);
                    
                    // 샘플 레이트 설정
                    if (isConvertedFile) {
                        // 변환된 FLAC 파일은 44100Hz 사용
                        configBuilder.setSampleRateHertz(44100);
                        log.info("변환된 FLAC 파일, 샘플 레이트를 44100Hz로 설정");
                    } else if (encoding == RecognitionConfig.AudioEncoding.LINEAR16) {
                        // WAV 파일은 샘플 레이트를 48000으로 설정
                        configBuilder.setSampleRateHertz(48000);
                        log.info("WAV 형식 감지, 샘플 레이트를 48000Hz로 설정");
                    } else if (encoding == RecognitionConfig.AudioEncoding.OGG_OPUS) {
                        // WebM/Opus는 48000Hz 사용
                        configBuilder.setSampleRateHertz(48000);
                        log.info("Opus 코덱 감지, 샘플 레이트를 48000Hz로 설정");
                    } else if (encoding == RecognitionConfig.AudioEncoding.FLAC) {
                        // FLAC는 44100Hz 사용
                        configBuilder.setSampleRateHertz(44100);
                        log.info("FLAC 형식 감지, 샘플 레이트를 44100Hz로 설정");
                    } else if (encoding == RecognitionConfig.AudioEncoding.MP3) {
                        // MP3는 일반적으로 44100Hz 사용
                        configBuilder.setSampleRateHertz(44100);
                        log.info("MP3 형식 감지, 샘플 레이트를 44100Hz로 설정");
                    }
                    // m4a 및 기타 형식은 샘플 레이트 자동 감지를 위해 설정하지 않음
                    
                    RecognitionConfig config = configBuilder.build();
                    
                    // 7. 음성 인식 요청
                    log.info("음성 인식 요청 시작");
                    RecognizeResponse response = speechClient.recognize(config, audio);
                    log.info("음성 인식 응답 받음");
                    
                    List<SpeechRecognitionResult> results = response.getResultsList();
                    
                    // 8. 결과 처리
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
            try {
                if (tempFile != null && !isConvertedFile) {
                    Files.deleteIfExists(tempFile);
                }
                if (convertedFile != null) {
                    Files.deleteIfExists(convertedFile);
                }
            } catch (IOException e) {
                log.warn("임시 파일 삭제 실패", e);
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
            } else if (contentType.contains("webm")) {
                // WebM은 보통 Opus 코덱을 사용하므로 OGG_OPUS로 처리
                log.info("WebM 형식 감지, Opus 코덱으로 처리");
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
        } else if ("m4a".equalsIgnoreCase(extension) || contentType.contains("m4a") || contentType.contains("mp4a")) {
            // m4a는 일반적으로 AAC 인코딩이지만, API가 직접 지원하지 않아 자동 감지 사용
            log.info("m4a 형식 감지, 인코딩 자동 감지 사용");
            return RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED;
        } else if ("webm".equalsIgnoreCase(extension)) {
            // WebM 확장자 처리
            log.info("WebM 확장자 감지, Opus 코덱으로 처리");
            return RecognitionConfig.AudioEncoding.OGG_OPUS;
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