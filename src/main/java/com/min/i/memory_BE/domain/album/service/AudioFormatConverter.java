package com.min.i.memory_BE.domain.album.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 오디오 파일 형식 변환을 담당하는 클래스입니다.
 * FFmpeg를 사용하여 지원되지 않는 형식(m4a, webm, mp3)을 지원되는 형식(flac, wav)으로 변환합니다.
 */
@Slf4j
@Component
public class AudioFormatConverter {

    // FFmpeg 명령어 경로 (환경에 따라 변경 필요)
    private static final String FFMPEG_PATH = "ffmpeg";
    
    // 지원되는 오디오 형식
    private static final String[] SUPPORTED_FORMATS = {"flac", "wav"};
    
    /**
     * 오디오 파일을 지원되는 형식으로 변환합니다.
     * 이미 지원되는 형식이면 원본 파일을 그대로 반환합니다.
     * 
     * @param audioFile 원본 오디오 파일
     * @return 변환된 파일의 경로 (변환 실패 시 null)
     */
    public Path convertToSupportedFormat(MultipartFile audioFile) {
        // 파일 확장자 확인
        String originalFilename = audioFile.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        // 이미 지원되는 형식이면 변환하지 않음
        if (isSupportedFormat(extension)) {
            try {
                Path tempFile = Files.createTempFile("original-", "." + extension);
                audioFile.transferTo(tempFile.toFile());
                log.info("이미 지원되는 형식({}), 변환 없이 반환", extension);
                return tempFile;
            } catch (IOException e) {
                log.error("파일 저장 중 오류 발생", e);
                return null;
            }
        }
        
        // 지원되지 않는 형식이면 FLAC으로 변환
        try {
            // 원본 파일을 임시 파일로 저장
            Path originalTempFile = Files.createTempFile("original-", "." + extension);
            audioFile.transferTo(originalTempFile.toFile());
            
            // 변환된 파일을 저장할 경로
            Path convertedFile = Files.createTempFile("converted-", ".flac");
            
            // FFmpeg를 사용하여 변환
            log.info("FFmpeg를 사용하여 {} 파일을 FLAC으로 변환 시작", extension);
            
            ProcessBuilder processBuilder = new ProcessBuilder(
                FFMPEG_PATH, 
                "-i", originalTempFile.toAbsolutePath().toString(),
                "-y",  // 기존 파일 덮어쓰기
                convertedFile.toAbsolutePath().toString()
            );
            
            // 오류 로그 리디렉션
            processBuilder.redirectErrorStream(true);
            
            // 프로세스 실행
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            // 원본 임시 파일 삭제
            Files.deleteIfExists(originalTempFile);
            
            if (exitCode != 0) {
                log.error("FFmpeg 변환 실패, 종료 코드: {}", exitCode);
                Files.deleteIfExists(convertedFile);
                return null;
            }
            
            log.info("파일 변환 완료: {} -> FLAC", extension);
            return convertedFile;
            
        } catch (IOException | InterruptedException e) {
            log.error("파일 변환 중 오류 발생", e);
            return null;
        }
    }
    
    /**
     * 주어진 형식이 지원되는 형식인지 확인합니다.
     */
    private boolean isSupportedFormat(String extension) {
        if (extension == null) return false;
        
        for (String format : SUPPORTED_FORMATS) {
            if (format.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
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
    
    /**
     * FFmpeg가 설치되어 있는지 확인합니다.
     * @return FFmpeg 설치 여부
     */
    public boolean isFFmpegAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(FFMPEG_PATH, "-version");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            log.error("FFmpeg 설치 확인 중 오류 발생", e);
            return false;
        }
    }
} 