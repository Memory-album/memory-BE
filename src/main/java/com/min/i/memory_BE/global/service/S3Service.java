package com.min.i.memory_BE.global.service;
import com.min.i.memory_BE.global.error.exception.FileValidationException;
import com.min.i.memory_BE.global.error.exception.S3Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class S3Service {
  private final S3Client s3Client;
  private final String bucketName;
  
  private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
    "image/jpeg", "image/jpg", "image/png", "image/gif"
  );
  
  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  
  public S3Service(S3Client s3Client,
    @Value("${spring.cloud.aws.s3.bucket}") String bucketName) {
    this.s3Client = s3Client;
    this.bucketName = bucketName;
  }
  
  // 앨범 이미지 업로드
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
      
      log.info("Album image uploaded successfully: {}", key);
      return getFileUrl(key);
      
    } catch (IOException e) {
      log.error("Album image upload failed: {}", e.getMessage());
      throw new S3Exception("앨범 이미지 업로드 중 오류가 발생했습니다");
    }
  }
  
  // 앨범 이미지 업데이트
  public String updateAlbumImage(MultipartFile file, Long albumId, String oldFileUrl) {
    validateImageFile(file);
    
    try {
      String newFileUrl = uploadAlbumImage(file, albumId);
      
      if (oldFileUrl != null && !oldFileUrl.isEmpty()) {
        deleteImage(oldFileUrl);
      }
      
      return newFileUrl;
    } catch (Exception e) {
      log.error("Album image update failed: {}", e.getMessage());
      throw new S3Exception("앨범 이미지 업데이트 중 오류가 발생했습니다");
    }
  }
  
  // 프로필 이미지 업로드
  public String uploadProfileImage(MultipartFile file, String userId) {
    validateImageFile(file);
    
    String fileName = generateFileName(file.getOriginalFilename());
    String key = String.format("users/%s/profile/%s", userId, fileName);
    
    try {
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .contentType(file.getContentType())
        .build();
      
      s3Client.putObject(putObjectRequest,
        RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
      
      log.info("Profile image uploaded successfully: {}", key);
      return getFileUrl(key);
      
    } catch (IOException e) {
      log.error("Profile image upload failed: {}", e.getMessage());
      throw new S3Exception("프로필 이미지 업로드 중 오류가 발생했습니다");
    }
  }
  
    public String uploadGroupImage(MultipartFile file, Long groupId) {
      validateImageFile(file);
      
      String fileName = generateFileName(file.getOriginalFilename());
      String key = String.format("groups/%d/image/%s", groupId, fileName);
      
      try {
        log.info("그룹 이미지 업로드 시작 - groupId: {}, fileName: {}",
          groupId, fileName);
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .contentType(file.getContentType())
          .build();
        
        s3Client.putObject(putObjectRequest,
          RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        
        String fileUrl = getFileUrl(key);
        log.info("그룹 이미지 업로드 성공 - groupId: {}, fileUrl: {}", groupId, fileUrl);
        return fileUrl;
        
      } catch (Exception e) {
        log.error("그룹 이미지 업로드 실패 - groupId: {}, error: {}",
          groupId, e.getMessage(), e);
        throw new S3Exception("그룹 이미지 업로드 중 오류 발생: " + e.getMessage());
      }
    }
  
  
  // 프로필 이미지 업데이트
  public String updateProfileImage(MultipartFile file, String userId, String oldFileUrl) {
    try {
      // 새 이미지 업로드
      String newFileUrl = uploadProfileImage(file, userId);
      
      // 이전 이미지 삭제
      if (oldFileUrl != null && !oldFileUrl.isEmpty() && !oldFileUrl.contains("default")) {
        deleteImage(oldFileUrl);
      }
      
      return newFileUrl;
    } catch (Exception e) {
      log.error("Profile image update failed: {}", e.getMessage());
      throw new S3Exception("프로필 이미지 업데이트 중 오류가 발생했습니다");
    }
  }
  
  // 이미지 삭제
  public void deleteImage(String fileUrl) {
    try {
      String key = extractKeyFromUrl(fileUrl);
      
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();
      
      s3Client.deleteObject(deleteObjectRequest);
      log.info("Image deleted successfully: {}", key);
      
    } catch (Exception e) {
      log.error("Image deletion failed: {}", e.getMessage());
      throw new S3Exception("이미지 삭제 중 오류가 발생했습니다");
    }
  }
  
  // 파일 검증 (공통 기능)
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
  
  // 파일 이름 생성 (공통 기능)
  private String generateFileName(String originalFilename) {
    return UUID.randomUUID().toString() + "-" + originalFilename;
  }
  
  // 파일 URL 생성 (공통 기능)
  private String getFileUrl(String key) {
    return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
  }
  
  // URL에서 키 추출 (공통 기능)
  private String extractKeyFromUrl(String fileUrl) {
    return fileUrl.substring(fileUrl.indexOf(".com/") + 5);
  }

  public String uploadThumbnail(MultipartFile file) {
    // S3에 파일 업로드 로직 구현
    // 업로드 후 파일의 URL 반환
    return "uploaded_thumbnail_url"; // 실제 URL로 변경 필요
  }

  /**
   * S3 프로토콜 URL을 객체 URL로 변환합니다.
   * 예: s3://bucket-name/path/to/file.jpg -> https://bucket-name.s3.amazonaws.com/path/to/file.jpg
   *
   * @param s3ProtocolUrl S3 프로토콜 URL (s3://bucket-name/path/to/file.jpg 형식)
   * @return 객체 URL (https://bucket-name.s3.amazonaws.com/path/to/file.jpg 형식)
   */
  public String convertS3ProtocolUrlToObjectUrl(String s3ProtocolUrl) {
    if (s3ProtocolUrl == null || s3ProtocolUrl.isEmpty()) {
      return null;
    }

    // S3 프로토콜 URL 형식 확인
    if (!s3ProtocolUrl.startsWith("s3://")) {
      // 이미 객체 URL 형식이면 그대로 반환
      if (s3ProtocolUrl.startsWith("http")) {
        return s3ProtocolUrl;
      }
      throw new IllegalArgumentException("유효한 S3 URL 형식이 아닙니다: " + s3ProtocolUrl);
    }

    // s3://bucket-name/path/to/file.jpg 형식에서 버킷 이름과 키 추출
    String withoutProtocol = s3ProtocolUrl.substring(5); // "s3://" 제거
    int firstSlashIndex = withoutProtocol.indexOf('/');
    
    if (firstSlashIndex == -1) {
      throw new IllegalArgumentException("S3 URL에 경로가 없습니다: " + s3ProtocolUrl);
    }
    
    String bucketName = withoutProtocol.substring(0, firstSlashIndex);
    String key = withoutProtocol.substring(firstSlashIndex + 1);
    
    // 객체 URL 생성
    String objectUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    
    log.info("S3 URL 변환: {} -> {}", s3ProtocolUrl, objectUrl);
    return objectUrl;
  }
}