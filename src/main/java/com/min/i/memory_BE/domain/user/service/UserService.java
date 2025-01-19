package com.min.i.memory_BE.domain.user.service;

import com.min.i.memory_BE.domain.user.dto.JwtAuthenticationResponse;
import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.UserStatus;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import com.min.i.memory_BE.global.security.jwt.JwtTokenProvider;
import com.min.i.memory_BE.global.service.S3Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.min.i.memory_BE.domain.user.event.EmailVerificationEvent;
import com.min.i.memory_BE.domain.user.dto.PasswordResetDto;
import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final S3Service s3Service;

    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, ApplicationEventPublisher eventPublisher, S3Service s3Service) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.eventPublisher = eventPublisher;
        this.s3Service = s3Service;
    }

    @Value("${jwt.secret}")
    private String secretKey;  // JWT 서명에 사용할 비밀키

    // JWT 토큰 발급
    public JwtAuthenticationResponse generateTokens(String email) {
        String token = jwtTokenProvider.generateToken(email);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);
        return new JwtAuthenticationResponse(token, refreshToken);
    }

    // 이메일 인증 확인 및 코드 검증 (JWT 사용)
    public String verifyEmail(String jwtToken, String inputVerificationCode) {
        try {
            // JWT 파싱하여 claims 추출
            var claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody();

            String email = claims.get("email", String.class);
            String verificationCode = claims.get("emailVerificationCode", String.class);
            LocalDateTime expirationTime = LocalDateTime.parse(claims.get("expirationTime", String.class));

            // 인증 코드 검증
            if (inputVerificationCode.equals(verificationCode) && LocalDateTime.now().isBefore(expirationTime)) {
                // 새 JWT 생성
                return Jwts.builder()
                        .claim("email", email)
                        .claim("emailVerificationCode", verificationCode)
                        .claim("expirationTime", expirationTime.toString())
                        .claim("isEmailVerified", true)
                        .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                        .compact();
            }
            
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 회원가입을 위한 최종 처리
    public void completeRegister(UserRegisterDto userRegisterDto, MultipartFile profileImage, String jwtToken) {
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

        // 최종 사용자로 build (이메일, 암호화된 비밀번호, 이름, 프로필 이미지 등)
        User newUser = User.builder()
                .email(email)  // JWT에서 가져온 이메일 사용
                .password(hashedPassword)  // 암호화된 비밀번호
                .name(userRegisterDto.getName())  // 사용자가 입력한 이름
                .emailVerified(true)  // 이메일 인증 완료
                .loginAttempts(0)  // 로그인 시도 횟수 초기화 (기본값 0이지만 명시적으로 설정 가능)
                .accountLocked(false)  // 계정 잠금 초기화
                .lastLoginAttempt(LocalDateTime.now())  // 마지막 로그인 시도 시간 현재 시간으로 설정
                .lockedUntil(null)  // 잠금 해제 시간 초기화 (null로 설정)
                .status(UserStatus.ACTIVE) // 사용자 상태 기본값 = 활성화
                .build();

        // 최종 사용자로 저장
        User savedUser = userRepository.save(newUser);
        
        // 프로필 이미지 처리
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String imageUrl = s3Service.uploadProfileImage(profileImage,
                  String.valueOf(savedUser.getId()));
                
                User updatedUser = savedUser.toBuilder()
                  .profileImgUrl(imageUrl)
                  .build();
                
                // BaseEntity 시간 정보 복사
                updatedUser.setCreatedAt(savedUser.getCreatedAt());
                updatedUser.setUpdatedAt(LocalDateTime.now());
                
                userRepository.save(updatedUser);
            } catch (Exception e) {
                logger.error("프로필 이미지 업로드 실패: {}", e.getMessage());
            }
        }
    
        
        // 환영 이메일 이벤트 발행
        eventPublisher.publishEvent(new EmailVerificationEvent(
          email,
          userRegisterDto.getName(),
          EmailVerificationEvent.EventType.WELCOME
        ));
    }
    

    // 이메일로 유저 조회
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);  // 이메일로 유저 조회, 없으면 null 반환
    }

    // 계정 잠금 여부 확인
    public boolean isAccountLocked(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return false;  // 사용자가 존재하지 않으면 잠기지 않음
        }

        // 계정 잠금 시간이 지나면 잠금 해제
        if (user.isAccountLocked() && user.getLockedUntil() != null && LocalDateTime.now().isAfter(user.getLockedUntil())) {
            unlockAccount(email);  // 잠금 해제
            return false;  // 계정 잠금 해제 후 다시 로그인 시도 가능
        }

        // 계정이 잠겼다면 true 반환
        return user.isAccountLocked();
    }

    // 로그인 시도 횟수를 증가시키고 계정을 잠그는 메서드
    public int incrementLoginAttempts(String email) {
        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    // 로그인 시도 횟수 증가 및 시간 갱신
                    int newAttempts = existingUser.getLoginAttempts() + 1;
                    
                    // 기존 사용자의 모든 필드를 유지하면서 필요한 필드만 업데이트
                    User updatedUser = existingUser.toBuilder()
                            .id(existingUser.getId())
                            .loginAttempts(newAttempts)
                            .lastLoginAttempt(LocalDateTime.now())
                            .accountLocked(newAttempts >= 5)
                            .lockedUntil(newAttempts >= 5 ? LocalDateTime.now().plusMinutes(30) : existingUser.getLockedUntil())
                            .build();

                    // BaseEntity 필드들 복사
                    updatedUser.setCreatedAt(existingUser.getCreatedAt());
                    updatedUser.setUpdatedAt(LocalDateTime.now());

                    userRepository.save(updatedUser);
                    return newAttempts;
                })
                .orElse(0);
    }

    
    // 계정 잠금 해제
    public void unlockAccount(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            // 기존 값을 기반으로 새 User 객체 생성하여 계정 잠금 해제
            User unlockedUser = user.toBuilder()
                    .accountLocked(false) // 계정 잠금 해제
                    .loginAttempts(0)  // 로그인 시도 횟수 초기화
                    .lockedUntil(null)  // 잠금 해제
                    .build();

            unlockedUser.setCreatedAt(user.getCreatedAt());
            unlockedUser.setUpdatedAt(LocalDateTime.now());

            userRepository.save(unlockedUser);  // 계정 잠금 해제
        }
    }

    // 사용자 정보 수정
    public User updateUser(String email, String newPassword, String name, String profileImgUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 새 비밀번호가 현재 비밀번호와 같은지 확인
        if (newPassword != null && passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        // 기존 사용자 정보를 바탕으로 변경할 부분만 수정하여 새로운 User 객체를 생성
        User updatedUser = user.toBuilder()
                .id(user.getId())
                .password(newPassword != null ? passwordEncoder.encode(newPassword) : user.getPassword())
                .name(name != null ? name : user.getName())
                .profileImgUrl(profileImgUrl != null ? profileImgUrl : user.getProfileImgUrl())
                .build();

        updatedUser.setCreatedAt(user.getCreatedAt());
        updatedUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(updatedUser);  // 수정된 사용자 정보 저장
    }

    // 사용자 비활성화
    public void deactivateUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 상태를 '비활성'으로 변경
        User updatedUser = user.toBuilder()
                .id(user.getId())
                .status(UserStatus.INACTIVE)
                .build();

        updatedUser.setCreatedAt(user.getCreatedAt());
        updatedUser.setUpdatedAt(LocalDateTime.now());

        userRepository.save(updatedUser);

        // 비활성화 알림 메일 발송
        eventPublisher.publishEvent(new EmailVerificationEvent(
            email, 
            user.getName(), 
            EmailVerificationEvent.EventType.ACCOUNT_DEACTIVATED
        ));
    }

    // 사용자 계정 활성화
    public void activateUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비활성화된 계정만 활성화할 수 있도록 처리
        if (user.getStatus() != UserStatus.INACTIVE) {
            throw new IllegalArgumentException("비활성화된 계정만 활성화할 수 있습니다.");
        }

        User updatedUser = user.toBuilder()
                .id(user.getId())
                .status(UserStatus.ACTIVE)
                .build();

        updatedUser.setCreatedAt(user.getCreatedAt());
        updatedUser.setUpdatedAt(LocalDateTime.now());

        userRepository.save(updatedUser);

        // 활성화 알림 메일 발송
        eventPublisher.publishEvent(new EmailVerificationEvent(
            email, 
            user.getName(), 
            EmailVerificationEvent.EventType.ACCOUNT_ACTIVATED
        ));
    }

    // 사용자 탈퇴 (계정 영구 삭제)
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        userRepository.delete(user);  // 사용자 계정 삭제
    }

    public String requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 이메일 이벤트 발행하고 JWT 받아오기
        EmailVerificationEvent event = new EmailVerificationEvent(
            email, 
            user.getName(), 
            EmailVerificationEvent.EventType.PASSWORD_RESET
        );
        eventPublisher.publishEvent(event);
        
        // EmailService에서 생성한 JWT 반환
        return event.getJwtToken();
    }
    
    public boolean verifyPasswordResetCode(String email, String verificationCode, String jwtToken) {
        try {
            // JWT 토큰을 검증
            var claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody();

            String storedEmail = claims.get("email", String.class);
            String storedCode = claims.get("emailVerificationCode", String.class);
            LocalDateTime expirationTime = LocalDateTime.parse(claims.get("expirationTime", String.class));
            String tokenType = claims.get("type", String.class);

            return email.equals(storedEmail) && 
                   verificationCode.equals(storedCode) &&
                   "PASSWORD_RESET".equals(tokenType) && 
                   LocalDateTime.now().isBefore(expirationTime);
        } catch (Exception e) {
            logger.error("비밀번호 재설정 코드 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    public void resetPassword(PasswordResetDto request, String jwtToken) {
        // JWT 토큰 검증
        var claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();

        String email = claims.get("email", String.class);
        
        // 이메일 일치 확인
        if (!email.equals(request.getEmail())) {
            throw new IllegalArgumentException("토큰의 이메일 정보가 일치하지 않습니다.");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 새 비밀번호 설정
        User updatedUser = user.toBuilder()
            .id(user.getId())
            .password(passwordEncoder.encode(request.getNewPassword()))
            .build();

        updatedUser.setCreatedAt(user.getCreatedAt());
        updatedUser.setUpdatedAt(LocalDateTime.now());

        userRepository.save(updatedUser);
    }
    
    @Transactional
    public User updateProfileImage(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        try {
            // 기존 프로필 이미지가 있다면 삭제
            if (user.getProfileImgUrl() != null && !user.getProfileImgUrl().isEmpty()) {
                s3Service.deleteImage(user.getProfileImgUrl());
            }
            
            // 새 프로필 이미지 업로드
            String imageUrl = s3Service.uploadProfileImage(file, String.valueOf(user.getId()));
            
            // 사용자 정보 업데이트
            User updatedUser = user.toBuilder()
              .profileImgUrl(imageUrl)
              .build();
            
            updatedUser.setCreatedAt(user.getCreatedAt());
            updatedUser.setUpdatedAt(LocalDateTime.now());
            
            return userRepository.save(updatedUser);
        } catch (Exception e) {
            logger.error("프로필 이미지 업데이트 실패: {}", e.getMessage());
            throw new IllegalArgumentException("프로필 이미지 업데이트에 실패했습니다: " + e.getMessage());
        }
    }
}
