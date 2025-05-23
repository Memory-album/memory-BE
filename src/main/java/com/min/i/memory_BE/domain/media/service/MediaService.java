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
import com.min.i.memory_BE.domain.user.dto.UserSimpleDto;
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
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

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
            
      
        LocalDateTime now = LocalDateTime.now();
        media.setCreatedAt(now);
        media.setUpdatedAt(now);

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
        // Use the method that fetches User entity eagerly
        List<Media> mediaList = mediaRepository.findByAlbumIdWithUserFetch(albumId);
        
        // 수동으로 정렬 및 제한
        return mediaList.stream()
                .sorted((m1, m2) -> m2.getCreatedAt().compareTo(m1.getCreatedAt())) // 생성일 기준 내림차순 정렬
                .limit(limit)
                .collect(Collectors.toList());
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
     * 개선된 버전: LazyInitializationException 방지 및 안전한 페이징 처리
     */
    @Transactional(readOnly = true)
    public Page<MediaResponseDto> getAllAlbumMediaWithAuth(Long groupId, Long albumId, Pageable pageable, User user) {
        try {
            log.info("미디어 조회 시작 - groupId: {}, albumId: {}, 페이지: {}, 사이즈: {}", 
                    groupId, albumId, pageable.getPageNumber(), pageable.getPageSize());
            
            // 그룹 존재 확인
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> {
                        log.error("그룹을 찾을 수 없음 - ID: {}", groupId);
                        return new EntityNotFoundException("Group not found with ID: " + groupId);
                    });
            
            log.info("그룹 조회 성공 - ID: {}, 이름: {}", group.getId(), group.getName());
            
            // 사용자가 그룹의 멤버인지 확인
            boolean isMember = group.getUserGroups().stream()
                    .anyMatch(userGroup -> userGroup.getUser().getId().equals(user.getId()));
            
            if (!isMember) {
                log.error("사용자가 그룹의 멤버가 아님 - 사용자 ID: {}, 그룹 ID: {}", user.getId(), groupId);
                throw new EntityNotFoundException("User is not a member of this group");
            }
            
            log.info("사용자 그룹 멤버십 확인 성공 - 사용자 ID: {}", user.getId());
            
            // 앨범이 해당 그룹에 속하는지 확인
            Album album = albumRepository.findByIdAndGroupId(albumId, groupId)
                    .orElseThrow(() -> {
                        log.error("앨범을 찾을 수 없음 - 앨범 ID: {}, 그룹 ID: {}", albumId, groupId);
                        return new EntityNotFoundException("Album not found in group with ID: " + albumId);
                    });
            
            log.info("앨범 조회 성공 - ID: {}, 제목: {}", album.getId(), album.getTitle());
            
            try {
                // 사용자(User) 정보를 함께 로드하는 쿼리 사용
                List<Media> mediaList = mediaRepository.findByAlbumIdAndGroupIdWithUserFetch(albumId, groupId);
                
                // 수동으로 안전하게 페이징 처리 (빈 목록 처리 및 인덱스 검증 추가)
                int start = (int) Math.min(pageable.getOffset(), mediaList.size());
                int end = Math.min((start + pageable.getPageSize()), mediaList.size());
                
                List<Media> pageContent = Collections.emptyList();
                if (start < end) {
                    pageContent = mediaList.subList(start, end);
                }
                
                // 트랜잭션 내에서 지연 로딩 되는 데이터를 모두 초기화하여 DTO 변환
                List<MediaResponseDto> dtoList = pageContent.stream()
                        .map(media -> convertToMediaResponseDto(media))
                        .collect(Collectors.toList());
                
                // DTO 객체로 페이지 생성 (지연 로딩 이슈 방지)
                PageImpl<MediaResponseDto> result = new PageImpl<>(dtoList, pageable, mediaList.size());
                log.info("미디어 조회 성공 - 총 {} 개의 미디어 아이템 반환", result.getTotalElements());
                return result;
                
            } catch (Exception e) {
                log.error("미디어 조회 중 데이터베이스 오류 발생: {}", e.getMessage(), e);
                throw new RuntimeException("Error querying media from database: " + e.getMessage(), e);
            }
        } catch (EntityNotFoundException e) {
            log.error("엔티티를 찾을 수 없음: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("미디어 조회 중 일반 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("Error retrieving album media: " + e.getMessage(), e);
        }
    }

    /**
     * 안전하게 Media 엔티티를 MediaResponseDto로 변환하는 메서드
     * 지연 로딩 관련 예외를 방지하기 위해 모든 필요한 관계를 명시적으로 초기화
     */
    private MediaResponseDto convertToMediaResponseDto(Media media) {
        // 명시적으로 스토리 컬렉션 초기화 (필요한 경우)
        String storyContent = null;
        if (media.getStories() != null && !media.getStories().isEmpty()) {
            storyContent = media.getStories().get(0).getContent();
        }
        
        // 사용자 정보 안전하게 처리
        UserSimpleDto userDto = null;
        if (media.getUploadedBy() != null) {
            userDto = UserSimpleDto.from(media.getUploadedBy());
        }
        
        return MediaResponseDto.builder()
                .id(media.getId())
                .fileUrl(media.getFileUrl())
                .fileType(media.getFileType())
                .originalFilename(media.getOriginalFilename())
                .fileSize(media.getFileSize())
                .thumbnailUrl(media.getThumbnailUrl())
                .uploadedBy(userDto)
                .createdAt(media.getCreatedAt())
                .story(storyContent)
                .build();
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