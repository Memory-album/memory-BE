package com.min.i.memory_BE.domain.album.service;

import com.min.i.memory_BE.domain.album.repository.AlbumRepository;
import com.min.i.memory_BE.domain.group.entity.Group;
import com.min.i.memory_BE.domain.group.repository.GroupRepository;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import com.min.i.memory_BE.global.error.exception.InvalidOperationException;
import com.min.i.memory_BE.global.service.S3Service;
import com.min.i.memory_BE.domain.album.dto.request.AlbumRequestDto;
import com.min.i.memory_BE.domain.album.dto.response.GroupAlbumListResponseDto;
import com.min.i.memory_BE.domain.album.entity.Album;
import com.min.i.memory_BE.domain.media.dto.response.MediaResponseDto;
import com.min.i.memory_BE.domain.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AlbumService {
    
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MediaService mediaService;
    private final S3Service s3Service;
    
    @Transactional
    public Album createAlbum(AlbumRequestDto request) {
        // 썸네일 업로드 (있는 경우에만)
        String thumbnailUrl = null;
        if (request.getThumbnailFile() != null && !request.getThumbnailFile().isEmpty()) {
            thumbnailUrl = s3Service.uploadThumbnail(request.getThumbnailFile());
        }
        
        // 사용자와 그룹 조회
        User user = null;
        
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getUserId()));
        }
        
        // 그룹 ID는 필수
        if (request.getGroupId() == null) {
            throw new EntityNotFoundException("Group ID is required");
        }
        
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + request.getGroupId()));
        
        // 앨범 생성 시 user와 group 연결
        Album album = Album.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnailUrl(thumbnailUrl)
                .theme(request.getTheme())
                .user(user)
                .group(group)
                .build();
        
        return albumRepository.save(album);
    }
    
    /**
     * 그룹 ID로 앨범 목록을 조회합니다.
     * 각 앨범마다 최근 미디어도 함께 조회합니다.
     *
     * @param groupId 그룹 ID
     * @param thumbnailCount 각 앨범 당 표시할 최근 미디어 수
     * @param user 요청한 사용자
     * @return 앨범 목록과 각 앨범의 최근 미디어 정보
     */
    public List<GroupAlbumListResponseDto> getAlbumsByGroupId(Long groupId, int thumbnailCount, User user) {
        // 1. 그룹 존재 확인 및 사용자 권한 확인
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + groupId));
        
        // 사용자가 그룹의 멤버인지 확인
        boolean isMember = group.getUserGroups().stream()
                .anyMatch(userGroup -> userGroup.getUser().getId().equals(user.getId()));
        
        if (!isMember) {
            throw new EntityNotFoundException("User is not a member of this group");
        }
        
        // 2. 그룹의 모든 앨범 조회
        List<Album> albums = albumRepository.findAllByGroupId(groupId);
        
        // 3. 각 앨범마다 최근 미디어 정보를 조회하여 응답 DTO 생성
        List<GroupAlbumListResponseDto> result = new ArrayList<>();
        
        for (Album album : albums) {
            // 앨범의 최근 미디어 조회
            List<MediaResponseDto> recentMedia = mediaService.getRecentMediaDtoByAlbumWithAuth(
                    album.getId(), 
                    thumbnailCount, 
                    user
            );
            
            // 응답 DTO 생성
            GroupAlbumListResponseDto dto = GroupAlbumListResponseDto.from(album, recentMedia);
            result.add(dto);
        }
        
        return result;
    }
    
    /**
     * 앨범 정보를 수정합니다.
     *
     * @param albumId 수정할 앨범 ID
     * @param request 수정할 앨범 정보
     * @return 수정된 앨범 객체
     */
    @Transactional
    public Album updateAlbum(Long albumId, AlbumRequestDto request) {
        log.info("앨범 수정 시작 - albumId: {}", albumId);
        
        // 앨범 존재 확인
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + albumId));
        
        // 권한 확인: 앨범 생성자 또는 그룹 관리자만 수정 가능
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getUserId()));
        
        // 앨범 생성자이거나 그룹 소유자/시니어인지 확인
        boolean isAlbumCreator = album.getUser() != null && album.getUser().getId().equals(user.getId());
        boolean isGroupAdmin = album.getGroup().getUserGroups().stream()
                .anyMatch(userGroup -> userGroup.getUser().getId().equals(user.getId()) && 
                         (userGroup.getRole().equals("OWNER") || userGroup.getRole().equals("SENIOR")));
        
        if (!isAlbumCreator && !isGroupAdmin) {
            throw new InvalidOperationException("You don't have permission to update this album");
        }
        
        // 제목 및 설명 업데이트
        if (request.getTitle() != null) {
            album.updateTitle(request.getTitle());
        }
        
        if (request.getDescription() != null) {
            album.updateDescription(request.getDescription());
        }
        
        // 테마 업데이트
        if (request.getTheme() != null) {
            album.updateTheme(request.getTheme());
        }
        
        // 썸네일 업데이트 (새 파일이 있는 경우)
        if (request.getThumbnailFile() != null && !request.getThumbnailFile().isEmpty()) {
            // 기존 썸네일이 있으면 삭제
            if (album.getThumbnailUrl() != null && !album.getThumbnailUrl().isEmpty()) {
                s3Service.deleteImage(album.getThumbnailUrl());
            }
            
            // 새 썸네일 업로드
            String newThumbnailUrl = s3Service.uploadThumbnail(request.getThumbnailFile());
            album.updateThumbnailUrl(newThumbnailUrl);
        }
        
        log.info("앨범 수정 완료 - albumId: {}, 제목: {}", album.getId(), album.getTitle());
        
        return albumRepository.save(album);
    }
    
    /**
     * 앨범을 삭제합니다. 앨범과 연결된 미디어는 유지됩니다.
     *
     * @param albumId 삭제할 앨범 ID
     * @param userId 요청한 사용자 ID
     */
    @Transactional
    public void deleteAlbum(Long albumId, Long userId) {
        log.info("앨범 삭제 시작 - albumId: {}, userId: {}", albumId, userId);
        
        // 앨범 존재 확인
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + albumId));
        
        // 권한 확인: 앨범 생성자 또는 그룹 관리자만 삭제 가능
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        // 앨범 생성자이거나 그룹 소유자/시니어인지 확인
        boolean isAlbumCreator = album.getUser() != null && album.getUser().getId().equals(user.getId());
        boolean isGroupAdmin = album.getGroup().getUserGroups().stream()
                .anyMatch(userGroup -> userGroup.getUser().getId().equals(user.getId()) && 
                         (userGroup.getRole().equals("OWNER") || userGroup.getRole().equals("SENIOR")));
        
        if (!isAlbumCreator && !isGroupAdmin) {
            throw new InvalidOperationException("You don't have permission to delete this album");
        }
        
        // 앨범 썸네일 삭제
        if (album.getThumbnailUrl() != null && !album.getThumbnailUrl().isEmpty()) {
            s3Service.deleteImage(album.getThumbnailUrl());
        }
        
        // 앨범 삭제
        albumRepository.delete(album);
        
        log.info("앨범 삭제 완료 - albumId: {}", albumId);
    }
}