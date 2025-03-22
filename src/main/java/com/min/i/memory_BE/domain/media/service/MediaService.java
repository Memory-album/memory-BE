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
import com.min.i.memory_BE.domain.media.dto.response.MediaResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaService {
    private final MediaRepository mediaRepository;
    private final AlbumRepository albumRepository;
    private final GroupRepository groupRepository;
    private final S3Service s3Service;
    private static final Logger log = LoggerFactory.getLogger(MediaService.class);

    /**
     * 미디어 업로드
     */
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

    /**
     * 미디어 수정
     */
    @Transactional
    public Media updateMedia(Long groupId, Long albumId, Long mediaId, MultipartFile file, User user) {
        // 권한 검증
        validateGroupMembership(groupId, user);

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

    /**
     * 미디어 삭제
     */
    @Transactional
    public void deleteMedia(Long groupId, Long albumId, Long mediaId, User user) {
        // 권한 검증
        validateGroupMembership(groupId, user);

        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new EntityNotFoundException("Media not found"));

        s3Service.deleteImage(media.getFileUrl());
        mediaRepository.delete(media);
    }

    /**
     * 그룹의 특정 앨범 미디어 조회 (페이징)
     */
    public Page<Media> getAlbumMedia(Long groupId, Long albumId, Pageable pageable) {
        return mediaRepository.findByAlbumIdAndGroupId(albumId, groupId, pageable);
    }

    /**
     * 앨범의 최근 미디어 조회 (메인페이지용)
     */
    public List<Media> getRecentMediaByAlbum(Long albumId, int limit) {
        // 앨범 존재 확인
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Album not found"));

        // 최신 미디어 조회 (생성일 기준 내림차순) - uploadedBy를 함께 로드
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Media> mediaPage = mediaRepository.findByAlbumIdWithUser(albumId, pageable);

        return mediaPage.getContent();
    }

    /**
     * 사용자가 해당 그룹의 멤버인지 확인
     */
    private void validateGroupMembership(Long groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        boolean isMember = group.getUserGroups().stream()
                .anyMatch(userGroup -> userGroup.getUser().getId().equals(user.getId()));

        if (!isMember) {
            throw new EntityNotFoundException("User is not a member of this group");
        }
    }

    /**
     * 그룹 내 특정 앨범의 전체 미디어 조회 (페이징) - 권한 검증 포함
     */
    public Page<Media> getAllAlbumMediaWithAuth(Long groupId, Long albumId, Pageable pageable, User user) {
        try {
            // 그룹 존재 확인
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new EntityNotFoundException("Group not found with ID: " + groupId));
            
            // 사용자가 그룹의 멤버인지 확인
            boolean isMember = group.getUserGroups().stream()
                    .anyMatch(userGroup -> userGroup.getUser().getId().equals(user.getId()));
            
            if (!isMember) {
                throw new EntityNotFoundException("User is not a member of this group");
            }
            
            // 앨범이 해당 그룹에 속하는지 확인
            Album album = albumRepository.findByIdAndGroupId(albumId, groupId)
                    .orElseThrow(() -> new EntityNotFoundException("Album not found in group with ID: " + albumId));
            
            // 앨범 내 모든 미디어 조회 (페이징)
            return mediaRepository.findByAlbumIdAndGroupId(albumId, groupId, pageable);
        } catch (Exception e) {
            // 로깅 및 예외 처리
            log.error("Error retrieving album media: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 사용자 권한 검증 후 앨범의 최근 미디어 조회 (DTO로 변환하여 반환)
     */
    @Transactional
    public List<MediaResponseDto> getRecentMediaDtoByAlbumWithAuth(Long albumId, int limit, User user) {
        // 앨범 존재 확인 및 그룹 확인
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Album not found"));

        // 앨범은 반드시 그룹에 속해야 함
        if (album.getGroup() == null) {
            throw new EntityNotFoundException("Album must belong to a group");
        }
        
        // 사용자가 앨범이 속한 그룹의 멤버인지 확인
        Long groupId = album.getGroup().getId();
        validateGroupMembership(groupId, user);

        // 최신 미디어 조회 및 DTO로 변환
        List<Media> mediaList = getRecentMediaByAlbum(albumId, limit);
        return MediaResponseDto.fromList(mediaList);
    }
}