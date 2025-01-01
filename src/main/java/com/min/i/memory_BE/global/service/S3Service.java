package com.min.i.memory_BE.global.service;

import com.min.i.memory_BE.global.error.exception.FileValidationException;
import com.min.i.memory_BE.global.error.exception.S3Exception;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
      
      log.info("File uploaded successfully: {}", key);
      return getFileUrl(key);
      
    } catch (IOException e) {
      log.error("File upload failed: {}", e.getMessage());
      throw new S3Exception("파일 업로드 중 오류가 발생했습니다");
    }
  }
  
  public String updateAlbumImage(MultipartFile file, Long albumId, String oldFileUrl) {
    validateImageFile(file);
    
    try {
      // 1. 새 파일 업로드
      String newFileUrl = uploadAlbumImage(file, albumId);
      
      // 2. 이전 파일 삭제
      if (oldFileUrl != null) {
        deleteImage(oldFileUrl);
      }
      
      return newFileUrl;
    } catch (Exception e) {
      log.error("File update failed: {}", e.getMessage());
      throw new S3Exception("파일 업데이트 중 오류가 발생했습니다");
    }
  }
  
  public void deleteImage(String fileUrl) {
    String key = extractKeyFromUrl(fileUrl);
    
    try {
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();
      
      s3Client.deleteObject(deleteObjectRequest);
      log.info("File deleted successfully: {}", key);
      
    } catch (Exception e) {
      log.error("File deletion failed: {}", e.getMessage());
      throw new S3Exception("파일 삭제 중 오류가 발생했습니다");
    }
  }
  
  private void validateImageFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new FileValidationException("파일이 비어있습니다");
    }
    
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new FileValidationException("파일 크기가 10MB를 초과합니다");
    }
    
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
      throw new FileValidationException("지원하지 않는 이미지 형식입니다");
    }
  }
  
  private String generateFileName(String originalFilename) {
    return UUID.randomUUID().toString() + "-" + originalFilename;
  }
  
  private String getFileUrl(String key) {
    return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
  }
  
  private String extractKeyFromUrl(String fileUrl) {
    return fileUrl.substring(fileUrl.indexOf(".com/") + 5);
  }
}