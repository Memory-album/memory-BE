package com.min.i.memory_BE.domain.user.service;

import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.dto.UserRegisterResultDto;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${jwt.secret}")
    private String secretKey;  // JWT 서명에 사용할 비밀키

    // 이메일 인증 확인 및 코드 검증 (JWT 사용)
    public String verifyEmail(String jwtToken, String inputVerificationCode) {
        try {
            // JWT 파싱하여 email, 인증 코드, 만료 시간 추출
            String email = Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes())
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody()
                    .get("email", String.class);

            String verificationCode = Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes())
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody()
                    .get("emailVerificationCode", String.class);

            LocalDateTime expirationTime = LocalDateTime.parse(Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes())
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody()
                    .get("expirationTime", String.class));

            // 인증 코드 검증
            if (inputVerificationCode.equals(verificationCode) && LocalDateTime.now().isBefore(expirationTime)) {

                // 기존 JWT를 기반으로 새 JWT를 생성하고, 이메일 인증 상태를 true로 변경
                String newJwt = Jwts.builder()
                        .claim("email", email)
                        .claim("emailVerificationCode", verificationCode)
                        .claim("expirationTime", expirationTime.toString())
                        .claim("isEmailVerified", true)  // 이메일 인증 완료 상태로 변경
                        .signWith(SignatureAlgorithm.HS256, getSecretKey())
                        .compact();

                return newJwt;
            }

            // 인증 실패 (만료된 인증 코드)
            if (LocalDateTime.now().isAfter(expirationTime)) {
                throw new IllegalArgumentException("인증 기한이 만료되었습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;  // JWT 파싱 오류나 검증 실패 시
        }

        return null;  // 인증 실패
    }

    // 회원가입을 위한 최종 처리
    public UserRegisterResultDto completeRegister(UserRegisterDto userRegisterDto, String jwtToken) {

        // JWT에서 이메일 가져오기
        String email = Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(jwtToken)
                .getBody()
                .get("email", String.class);

        // 이메일 인증이 완료된 상태에서만 최종 가입 진행
        boolean isEmailVerified = Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(jwtToken)
                .getBody()
                .get("isEmailVerified", Boolean.class);

        if (!isEmailVerified) {
            throw new IllegalArgumentException("이메일 인증을 먼저 완료해야 합니다.");
        }

        // 비밀번호 암호화
        String hashedPassword = passwordEncoder.encode(userRegisterDto.getPassword());
        userRegisterDto.setPassword(hashedPassword);  // 암호화된 비밀번호로 덮어쓰기

        // 최종 사용자로 업데이트 (이메일, 암호화된 비밀번호, 이름, 프로필 이미지 등)
        User newUser = User.builder()
                .email(email)  // JWT에서 가져온 이메일 사용
                .password(hashedPassword)  // 암호화된 비밀번호
                .name(userRegisterDto.getName())  // 사용자가 입력한 이름
                .profileImageUrl(userRegisterDto.getProfileImgUrl())  // 사용자가 입력한 프로필 이미지 URL
                .emailVerified(true)  // 이메일 인증 완료
                .build();

        // 최종 사용자로 저장
        userRepository.save(newUser);

        // 결과 반환 (DTO 반환)
        UserRegisterResultDto result = new UserRegisterResultDto();
        result.setMessage("회원가입 성공");
        result.setStatus("success");
        return result;
    }

    // JWT 비밀키를 Base64 URL-safe로 인코딩
    private String getSecretKey() {
        return Base64.getEncoder().encodeToString(secretKey.getBytes());
    }
}
