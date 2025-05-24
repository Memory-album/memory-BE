package com.min.i.memory_BE.global.security.jwt;

import com.min.i.memory_BE.global.config.SecurityConfig;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.io.Decoders;
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

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String secretKeyString;
    
    private Key secretKey;

    @PostConstruct
    public void init() {
        try {
            // Base64로 인코딩된 키인 경우
            if (secretKeyString.matches("^[A-Za-z0-9+/]*={0,2}$") && secretKeyString.length() >= 86) {
                byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
                this.secretKey = Keys.hmacShaKeyFor(keyBytes);
            } else {
                // 일반 문자열인 경우 - 최소 64바이트 이상 확인
                byte[] keyBytes = secretKeyString.getBytes();
                if (keyBytes.length < 64) {
                    logger.warn("JWT 키가 짧음.");
                    this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
                } else {
                    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
                }
            }
            logger.info("JWT 키발급 성공");
        } catch (Exception e) {
            logger.error("실패 새로 만드세요", e);
            this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        }
    }

    private final long EXPIRATION_TIME = 86400000; // 24시간
    private final long REFRESH_EXPIRATION_TIME = 2592000000L; // 30일

    // JWT 토큰 생성
    public String generateToken(String email) {
        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        return token;
    }

    // Refresh Token 생성
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }


    // JWT에서 이메일 가져오기
    public String getEmailFromToken(String token) {
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
