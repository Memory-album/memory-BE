package com.min.i.memory_BE.domain.album.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 오디오 파일 형식 변환기 (Cross-Platform + 안정화)
 */
@Slf4j
@Component
public class AudioFormatConverter {

    private String resolvedFfmpegPath;
    //변수선언
    private boolean ffmpegAvailable = false;
   // 수정
    private static final String[] SUPPORTED_FORMATS = {"flac"}; 

//    @PostConstruct
//    public void init() {
//        try {
//            String osName = System.getProperty("os.name").toLowerCase();
//            String ffmpegResourcePath;
//
//            if (osName.contains("win")) {
//                ffmpegResourcePath = "bin/ffmpeg-win.exe";
//            } else if (osName.contains("mac")) {
//                ffmpegResourcePath = "bin/ffmpeg-mac";
//            } else if (osName.contains("nux") || osName.contains("nix")) {
//                ffmpegResourcePath = "bin/ffmpeg-linux";
//            } else {
//                throw new UnsupportedOperationException("지원하지 않는 OS: " + osName);
//            }
//
//            File ffmpegFile = new ClassPathResource(ffmpegResourcePath).getFile();
//            if (!ffmpegFile.exists()) {
//                throw new IllegalStateException("ffmpeg 실행파일이 없습니다: " + ffmpegResourcePath);
//            }
//
//            resolvedFfmpegPath = ffmpegFile.getAbsolutePath();
//            if (!osName.contains("win")) {
//                ffmpegFile.setExecutable(true);
//            }
//
//            log.info("FFmpeg 경로 설정 완료: {}", resolvedFfmpegPath);
//
//            // ffmpeg 버전 로그 (실전 디버깅에 유용)
//            ProcessBuilder pb = new ProcessBuilder(resolvedFfmpegPath, "-version");
//            pb.redirectErrorStream(true);
//            Process p = pb.start();
//            p.waitFor();
//            log.info("FFmpeg 버전 확인 완료");
//
//        } catch (Exception e) {
//            log.error("FFmpeg 로딩 실패", e);
//            resolvedFfmpegPath = null;
//        }
//    }

    //git lfs 바이너리 횟수가 초기화되어 우선 시스템 파일 내부에서 확인
    @PostConstruct
    public void init() {
        try {
            // 시스템 ffmpeg 사용 가능 여부 확인
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exitCode = p.waitFor();

            if (exitCode == 0) {
                ffmpegAvailable = true;
                log.info("시스템 FFmpeg 사용 가능");
            } else {
                log.warn("시스템 FFmpeg 실행 실패");
            }

        } catch (Exception e) {
            log.warn("FFmpeg가 시스템에 설치되어 있지 않습니다. 오디오 변환 기능이 비활성화됩니다: {}", e.getMessage());
            ffmpegAvailable = false;
        }
    }

    public Path convertToSupportedFormat(MultipartFile audioFile) {
        if (resolvedFfmpegPath == null) {
            log.error("FFmpeg 경로 미설정, 변환 불가");
            return null;
        }

        String originalFilename = audioFile.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        if (isSupportedFormat(extension)) {
            return saveOriginal(audioFile, extension);
        }

        return convertWithFfmpeg(audioFile, extension);
    }

    private Path saveOriginal(MultipartFile audioFile, String extension) {
        try {
            Path tempFile = Files.createTempFile("original-", "." + extension);
            audioFile.transferTo(tempFile.toFile());
            log.info("지원되는 형식 {} -> 변환없이 사용", extension);
            return tempFile;
        } catch (IOException e) {
            log.error("원본 파일 저장 실패", e);
            return null;
        }
    }

    private Path convertWithFfmpeg(MultipartFile audioFile, String extension) {
        try {
            Path originalTempFile = Files.createTempFile("original-", "." + extension);
            audioFile.transferTo(originalTempFile.toFile());
            Path convertedFile = Files.createTempFile("converted-", ".flac");

            log.info("FFmpeg 변환 시작: {} -> flac", extension);

            ProcessBuilder pb = new ProcessBuilder(
                resolvedFfmpegPath, "-i", originalTempFile.toAbsolutePath().toString(),
                "-ac", "1", "-ar", "44100", "-sample_fmt", "s16", "-y",
                convertedFile.toAbsolutePath().toString()
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();

            Files.deleteIfExists(originalTempFile);

            if (exitCode != 0) {
                log.error("FFmpeg 변환 실패. 코드: {}", exitCode);
                Files.deleteIfExists(convertedFile);
                return null;
            }

            log.info("변환 완료: {} -> flac", extension);
            return convertedFile;

        } catch (IOException | InterruptedException e) {
            log.error("FFmpeg 변환 중 오류", e);
            return null;
        }
    }

    private boolean isSupportedFormat(String extension) {
        if (extension == null) return false;
        for (String format : SUPPORTED_FORMATS) {
            if (format.equalsIgnoreCase(extension)) return true;
        }
        return false;
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "tmp";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) return "tmp";
        return filename.substring(lastDotIndex + 1);
    }

    public boolean isFFmpegAvailable() {
        if (resolvedFfmpegPath == null) return false;

        try {
            ProcessBuilder pb = new ProcessBuilder(resolvedFfmpegPath, "-version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exitCode = p.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            log.error("FFmpeg 확인 실패", e);
            return false;
        }
    }
}
