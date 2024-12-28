package com.min.i.memory_BE.global.service;

import com.min.i.memory_BE.global.error.ErrorCode;
import com.min.i.memory_BE.global.error.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;

@Service
public class S3Service {
  private final S3Client s3Client;
  private final String bucketName;
  
  private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
    "image/jpeg", "image/jpg", "image/png", "image/gif"
  );
  
  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
  
  public S3Service(S3Client s3Client,
    @Value("${spring.cloud.aws.s3.bucket}") String bucketName) {
    this.s3Client = s3Client;
    this.bucketName = bucketName;
  }
  
  /**
   * 앨범 이미지 업로드
   */
  public String uploadAlbumImage(MultipartFile file, Long albumId) {
    validateImageFile(file);
    
    String fileName = generateFileName(file.getOriginalFilename());
    String key = String.format("albums/%d/original/%s", albumId, fileName);
    
    try {
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .contentType(file.getContentType())
        .build();
      
      s3Client.putObject(putObjectRequest,
        RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
      
      return getFileUrl(key);
    } catch (IOException e) {
      throw new ApiException("파일 업로드 중 오류가 발생했습니다", ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
  
  /**
   * 이미지 삭제
   */
  public void deleteImage(String fileUrl) {
    String key = extractKeyFromUrl(fileUrl);
    
    try {
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();
      
      s3Client.deleteObject(deleteObjectRequest);
    } catch (Exception e) {
      throw new ApiException("파일 삭제 중 오류가 발생했습니다", ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
  
  /**
   * 파일 유효성 검사
   */
  private void validateImageFile(MultipartFile file) {
    // 파일 존재 여부 확인
    if (file.isEmpty()) {
      throw new ApiException("파일이 비어있습니다", ErrorCode.INVALID_INPUT_VALUE);
    }
    
    // 파일 크기 검사
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new ApiException("파일 크기가 10MB를 초과합니다", ErrorCode.INVALID_INPUT_VALUE);
    }
    
    // 파일 타입 검사
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
      throw new ApiException("지원하지 않는 이미지 형식입니다", ErrorCode.INVALID_INPUT_VALUE);
    }
  }
  
  /**
   * 고유한 파일명 생성
   */
  private String generateFileName(String originalFilename) {
    return UUID.randomUUID().toString() + "-" + originalFilename;
  }
  
  /**
   * 파일 URL 생성
   */
  private String getFileUrl(String key) {
    return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
  }
  
  /**
   * URL에서 키 추출
   */
  private String extractKeyFromUrl(String fileUrl) {
    return fileUrl.substring(fileUrl.indexOf(".com/") + 5);
  }
}