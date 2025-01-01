package com.min.i.memory_BE.domain.media.service;

import com.min.i.memory_BE.domain.album.entity.Album;
import com.min.i.memory_BE.domain.album.repository.AlbumRepository;
import com.min.i.memory_BE.domain.group.entity.Group;
import com.min.i.memory_BE.domain.group.repository.GroupRepository;
import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.media.enums.MediaType;
import com.min.i.memory_BE.domain.media.repository.MediaRepository;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import com.min.i.memory_BE.global.service.S3Service;
import java.nio.file.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaService {
  private final MediaRepository mediaRepository;
  private final AlbumRepository albumRepository;
  private final GroupRepository groupRepository;
  private final S3Service s3Service;
  
  @Transactional
  public Media uploadMedia(MultipartFile file, Long groupId, Long albumId, User uploadedBy) {
    // 1. 그룹 접근 권한 확인
    Group group = groupRepository.findById(groupId)
      .orElseThrow(() -> new EntityNotFoundException("Group not found"));
    
    // 2. 앨범이 해당 그룹에 속하는지 확인
    Album album = albumRepository.findByIdAndGroupId(albumId, groupId)
      .orElseThrow(() -> new EntityNotFoundException("Album not found in group"));
    
    // 3. S3에 파일 업로드
    String fileUrl = s3Service.uploadAlbumImage(file, albumId);
    
    // 4. Media 엔티티 생성 및 저장
    Media media = Media.builder()
      .fileUrl(fileUrl)
      .fileType(MediaType.IMAGE)
      .originalFilename(file.getOriginalFilename())
      .fileSize(file.getSize())
      .album(album)
      .uploadedBy(uploadedBy)
      .build();
    
    return mediaRepository.save(media);
  }
  
  @Transactional
  public Media updateMedia(Long groupId, Long albumId, Long mediaId, MultipartFile file, User user) {
    Media existingMedia = mediaRepository.findById(mediaId)
      .orElseThrow(() -> new EntityNotFoundException("Media not found"));
    
    // S3에 새 파일 업로드 및 이전 파일 삭제
    String newFileUrl = s3Service.updateAlbumImage(file, albumId, existingMedia.getFileUrl());
    
    Media updatedMedia = Media.builder()
      .fileUrl(newFileUrl)
      .fileType(MediaType.IMAGE)
      .originalFilename(file.getOriginalFilename())
      .fileSize(file.getSize())
      .album(existingMedia.getAlbum())
      .uploadedBy(existingMedia.getUploadedBy())
      .page(existingMedia.getPage())
      .build();
    
    return mediaRepository.save(updatedMedia);
  }
  
  @Transactional
  public void deleteMedia(Long groupId, Long albumId, Long mediaId, User user) {
    Media media = mediaRepository.findById(mediaId)
      .orElseThrow(() -> new EntityNotFoundException("Media not found"));
    
    
    s3Service.deleteImage(media.getFileUrl());
    mediaRepository.delete(media);
  }
  
  // 그룹의 특정 앨범 미디어 조회 (페이징)
  public Page<Media> getAlbumMedia(Long groupId, Long albumId, Pageable pageable) {
    return mediaRepository.findByAlbumIdAndGroupId(albumId, groupId, pageable);
  }
}
