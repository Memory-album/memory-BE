package com.min.i.memory_BE.global.security.jwt;

import com.min.i.memory_BE.global.config.SecurityConfig;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${jwt.secret}")
    private String secretKeyString;
    
    private Key secretKey;
    
    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKeyString.getBytes();
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private final long EXPIRATION_TIME = 86400000; // 24시간
    private final long REFRESH_EXPIRATION_TIME = 2592000000L; // 30일

    // JWT 토큰 생성
    public String generateToken(String username) {
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        return token;
    }

    // Refresh Token 생성
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }


    // JWT에서 사용자 이름 가져오기
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("유효하지 않은 토큰입니다. (만료됨): {}. 이 토큰은 {} 시간 전에 만료되었습니다.",
                    token, (System.currentTimeMillis() - e.getClaims().getExpiration().getTime()) / 1000);
        } catch (Exception e) {
            logger.error("유효하지 않은 토큰입니다. 예상치 못한 오류가 발생했습니다: {}. 오류 메시지: {}",
                    token, e.getMessage());
        }
        return false;
    }

    // 리프레시 토큰 검사
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(refreshToken);
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("리프레시 토큰이 만료되었습니다: {}. 만료 시간: {}",
                    refreshToken, e.getClaims().getExpiration());
        } catch (Exception e) {
            logger.error("리프레시 토큰이 유효하지 않습니다: {}. 오류 메시지: {}",
                    refreshToken, e.getMessage());
        }
        return false;
    }
}
