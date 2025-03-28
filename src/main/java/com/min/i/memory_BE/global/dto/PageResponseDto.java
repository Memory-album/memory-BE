package com.min.i.memory_BE.global.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 페이징 처리된 API 응답을 위한 공통 DTO 클래스
 * @param <T> 페이징 데이터 타입
 */
@Getter
@Builder
public class PageResponseDto<T> {
    private List<T> content;       // 페이지 데이터
    private int pageNumber;        // 현재 페이지 번호 (0부터 시작)
    private int pageSize;          // 페이지 크기
    private long totalElements;    // 전체 데이터 수
    private int totalPages;        // 전체 페이지 수
    private boolean first;         // 첫 페이지 여부
    private boolean last;          // 마지막 페이지 여부

    /**
     * Spring Data JPA의 Page 객체를 PageResponseDto로 변환합니다.
     *
     * @param page JPA Page 객체
     * @param converter 엔티티를 DTO로 변환하는 함수
     * @param <E> 엔티티 타입
     * @param <D> DTO 타입
     * @return 변환된 PageResponseDto 객체
     */
    public static <E, D> PageResponseDto<D> of(Page<E> page, Function<E, D> converter) {
        List<D> content = page.getContent().stream()
                .map(converter)
                .collect(Collectors.toList());

        return PageResponseDto.<D>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}