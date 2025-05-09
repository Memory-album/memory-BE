package com.min.i.memory_BE.domain.album.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Google Speech-to-Text 서비스
 * - Credentials 캐싱
 * - sample_rate 자동화 (WAV 오류 해결)
 * - 변환된 파일, 원본 파일 모두 대응
 */
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

    private GoogleCredentials cachedCredentials;
    private boolean ffmpegAvailable = false;

    @PostConstruct
    public void init() {
        try {
            // 환경 변수에서 자격 증명 파일 경로를 가져오거나 리소스에서 로드
            String googleCredsEnv = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (googleCredsEnv != null && !googleCredsEnv.isEmpty()) {
                log.info("환경 변수에서 Google 자격 증명 경로 사용: {}", googleCredsEnv);
                // 환경 변수가 설정되어 있으면 해당 파일에서 자격 증명 로드
                cachedCredentials = GoogleCredentials.fromStream(new FileInputStream(googleCredsEnv));
            } else {
                // 환경 변수가 없으면 리소스에서 로드
                Resource credentialsResource = resourceLoader.getResource(credentialsFilePath);
                if (!credentialsResource.exists()) {
                    throw new FileNotFoundException("Google 인증파일 없음: " + credentialsFilePath);
                }
                
                // 스트림으로 직접 읽기 (getFile() 대신)
                InputStream credentialsStream = credentialsResource.getInputStream();
                cachedCredentials = GoogleCredentials.fromStream(credentialsStream);
                log.info("리소스에서 Google 자격 증명 로드 완료: {}", credentialsFilePath);
            }
            
            ffmpegAvailable = audioFormatConverter.isFFmpegAvailable();
            log.info("FFmpeg 상태: {}", ffmpegAvailable ? "사용 가능" : "사용 불가");

        } catch (Exception e) {
            log.error("초기화 실패", e);
            throw new IllegalStateException("SpeechToTextService 초기화 실패", e);
        }
    }

    public String convertSpeechToText(MultipartFile audioFile) {
        Path tempFile = null;
        Path convertedFile = null;
        boolean isConvertedFile = false;
        
        try {
            log.info("음성파일 처리 시작: {} ({})", audioFile.getOriginalFilename(), audioFile.getContentType());
            
            String extension = getFileExtension(audioFile.getOriginalFilename());

            // 형식 변환
            if (ffmpegAvailable) {
                convertedFile = audioFormatConverter.convertToSupportedFormat(audioFile);
                if (convertedFile != null) {
                    tempFile = convertedFile;
                    isConvertedFile = true;
                    extension = "flac";
                    log.info("변환 성공: {} → flac", audioFile.getOriginalFilename());
                } else {
                    log.warn("변환 실패, 원본 사용");
                }
            }

            if (tempFile == null) {
            tempFile = Files.createTempFile("speech-", "." + extension);
            audioFile.transferTo(tempFile.toFile());
            }
            
            // STT 호출 준비
            byte[] audioBytes = Files.readAllBytes(tempFile);
                RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(ByteString.copyFrom(audioBytes))
                    .build();
                
            RecognitionConfig.Builder configBuilder = RecognitionConfig.newBuilder()
                    .setEncoding(resolveEncoding(audioFile, isConvertedFile, extension))
                    .setLanguageCode(language);

            // Sample rate 설정
            if (isConvertedFile || extension.equalsIgnoreCase("flac")) {
                configBuilder.setSampleRateHertz(44100);
            } else if (extension.equalsIgnoreCase("mp3")) {
                configBuilder.setSampleRateHertz(44100);
            } else if (extension.equalsIgnoreCase("webm") || extension.equalsIgnoreCase("ogg") || extension.equalsIgnoreCase("opus")) {
                configBuilder.setSampleRateHertz(48000);
            } else if (extension.equalsIgnoreCase("wav")) {
                log.info("WAV 감지 → sample_rate 자동 감지");
                // WAV는 설정 안함 (Google이 header 읽음)
            }

            RecognitionConfig config = configBuilder.build();

            // Google STT 요청
            try (SpeechClient speechClient = SpeechClient.create(SpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> cachedCredentials)
                    .build())) {

                RecognizeResponse response = speechClient.recognize(config, audio);
                List<SpeechRecognitionResult> results = response.getResultsList();
                
                if (results.isEmpty()) {
                    log.warn("STT 결과 없음");
                    return "";
                }
                
                StringBuilder transcription = new StringBuilder();
                for (SpeechRecognitionResult result : results) {
                    SpeechRecognitionAlternative alt = result.getAlternativesList().get(0);
                    transcription.append(alt.getTranscript());
                    log.info("인식 결과: {}", alt.getTranscript());
                }

                return transcription.toString();
            }

        } catch (Exception e) {
            log.error("STT 처리 중 오류", e);
            throw new RuntimeException("STT 처리 실패: " + e.getMessage(), e);
        } finally {
                try {
                if (tempFile != null) Files.deleteIfExists(tempFile);
                if (convertedFile != null) Files.deleteIfExists(convertedFile);
                } catch (IOException e) {
                log.warn("임시파일 삭제 실패", e);
            }
        }
    }

    private RecognitionConfig.AudioEncoding resolveEncoding(MultipartFile file, boolean isConverted, String extension) {
        if (isConverted) return RecognitionConfig.AudioEncoding.FLAC;

        String contentType = file.getContentType() != null ? file.getContentType() : "";
        if (contentType.contains("wav")) return RecognitionConfig.AudioEncoding.LINEAR16;
        if (contentType.contains("flac")) return RecognitionConfig.AudioEncoding.FLAC;
        if (contentType.contains("mp3")) return RecognitionConfig.AudioEncoding.MP3;
        if (contentType.contains("ogg") || contentType.contains("opus")) return RecognitionConfig.AudioEncoding.OGG_OPUS;
        if (contentType.contains("webm")) return RecognitionConfig.AudioEncoding.OGG_OPUS;

        if (extension.equalsIgnoreCase("wav")) return RecognitionConfig.AudioEncoding.LINEAR16;
        if (extension.equalsIgnoreCase("flac")) return RecognitionConfig.AudioEncoding.FLAC;
        if (extension.equalsIgnoreCase("mp3")) return RecognitionConfig.AudioEncoding.MP3;
        if (extension.equalsIgnoreCase("ogg") || extension.equalsIgnoreCase("opus")) return RecognitionConfig.AudioEncoding.OGG_OPUS;
        if (extension.equalsIgnoreCase("webm")) return RecognitionConfig.AudioEncoding.OGG_OPUS;

        return RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED;
    }
    
    private String getFileExtension(String filename) {
        if (filename == null) return "tmp";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) return "tmp";
        return filename.substring(lastDotIndex + 1);
    }
}