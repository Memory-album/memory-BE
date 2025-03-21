package com.min.i.memory_BE.domain.album.service;

import com.min.i.memory_BE.domain.album.repository.AlbumRepository;
import com.min.i.memory_BE.domain.group.entity.Group;
import com.min.i.memory_BE.domain.group.repository.GroupRepository;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import com.min.i.memory_BE.global.service.S3Service;
import com.min.i.memory_BE.domain.album.dto.request.AlbumRequestDto;
import com.min.i.memory_BE.domain.album.entity.Album;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlbumService {
    
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final S3Service s3Service;
    
    @Transactional
    public Album createAlbum(AlbumRequestDto request) {
        // 썸네일 업로드
        String thumbnailUrl = s3Service.uploadThumbnail(request.getThumbnailFile());
        
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
}