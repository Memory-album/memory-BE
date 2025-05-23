package com.min.i.memory_BE.global.config;

import com.min.i.memory_BE.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import com.min.i.memory_BE.domain.user.security.CustomUserDetails;
import com.min.i.memory_BE.domain.user.service.MyUserDetailsService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    //JWT를 사용하여 요청이 들어올 때마다 인증을 처리하는 필터 - 인증된 사용자인지 확인하고, 그에 맞는 권한을 부여함.

    private final JwtTokenProvider jwtTokenProvider;  // JwtTokenProvider를 주입받음
    private final MyUserDetailsService userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    @Override //HTTP 요청을 처리
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 쿠키에서 JWT 토큰 추출
            String jwtToken = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwtToken".equals(cookie.getName())) {
                        jwtToken = cookie.getValue();
                        break;
                    }
                }
            }

            // 토큰이 있고 유효한 경우에만 인증 처리
            if (jwtToken != null && jwtTokenProvider.validateToken(jwtToken)) {
                String email = jwtTokenProvider.getEmailFromToken(jwtToken);
                
                // CustomUserDetails 객체 생성
                CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
                
                // Authentication 객체 생성 시 CustomUserDetails 사용
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                
            SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다", email);
            }

            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            logger.error("Security Context에 사용자 인증 정보를 설정할 수 없습니다: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }
}
