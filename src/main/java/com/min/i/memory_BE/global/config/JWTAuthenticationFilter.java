package com.min.i.memory_BE.global.config;

import com.min.i.memory_BE.domain.user.service.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JWTAuthenticationFilter  extends OncePerRequestFilter {

    //JWT를 사용하여 요청이 들어올 때마다 인증을 처리하는 필터 - 인증된 사용자인지 확인하고, 그에 맞는 권한을 부여함.

    private final JwtTokenProvider jwtTokenProvider;  // JwtTokenProvider를 주입받음

    @Autowired
    public JWTAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider; //JWT 토큰을 발급하고 검증
    }

    @Override //HTTP 요청을 처리
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // "/auth/login" 경로에 대해서는 필터를 적용하지 않음
        if ("/auth/login".equals(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        //요청 헤더에서 JWT 토큰을 추출
        String token = getTokenFromRequest(request);

        //추출한 토큰이 유효하다면, JwtTokenProvider를 사용하여 토큰을 검증, 토큰에서 사용자 정보를 추출
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);

            //추출한 사용자 정보로 UsernamePasswordAuthenticationToken 생성
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.singletonList(new SimpleGrantedAuthority("USER"))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            //Spring Security의 SecurityContext에 설정하여 인증된 사용자로 처리
            //이후 이 정보를 바탕으로 인증 및 권한 부여가 이루어짐! = 즉 로그인 여부로 특정 페이지 접근 등을 제한 할 수 있음
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }

    //요청 헤더에서 "Bearer "로 시작하는 JWT 토큰을 추출
    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        //"Bearer "라는 접두어 제거, 실제 토큰 반환
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7); // "Bearer " 제거
        }
        return null;
    }
}
