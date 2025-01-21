package com.min.i.memory_BE.domain.user.service;

import com.min.i.memory_BE.domain.user.dto.JwtAuthenticationResponse;
import com.min.i.memory_BE.domain.user.dto.PasswordResetDto;
import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.UserStatus;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import com.min.i.memory_BE.domain.user.event.EmailVerificationEvent;
import com.min.i.memory_BE.global.error.exception.S3Exception;
import com.min.i.memory_BE.global.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.min.i.memory_BE.global.service.S3Service;
import java.time.LocalDateTime;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final S3Service s3Service;
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Autowired
    public UserService(UserRepository userRepository,
      @Lazy PasswordEncoder passwordEncoder,
      JwtTokenProvider jwtTokenProvider,
      ApplicationEventPublisher eventPublisher,
      S3Service s3Service) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.eventPublisher = eventPublisher;
        this.s3Service = s3Service;
    }
    
    public JwtAuthenticationResponse generateTokens(String email) {
        String token = jwtTokenProvider.generateToken(email);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);
        return new JwtAuthenticationResponse(token, refreshToken);
    }
    
    // verifyEmail 메서드에서 signWith 부분 수정
    public String verifyEmail(String jwtToken, String inputVerificationCode) {
        try {
            Claims claims = validateRegistrationToken(jwtToken);
            String email = claims.get("email", String.class);
            String verificationCode = claims.get("emailVerificationCode", String.class);
            LocalDateTime expirationTime = LocalDateTime.parse(claims.get("expirationTime", String.class));
            
            if (inputVerificationCode.equals(verificationCode) && LocalDateTime.now().isBefore(expirationTime)) {
                return Jwts.builder()
                  .claim("email", email)
                  .claim("emailVerificationCode", verificationCode)
                  .claim("expirationTime", expirationTime.toString())
                  .claim("isEmailVerified", true)
                  .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS512)  // 수정된 부분
                  .compact();
            }
            return null;
        } catch (Exception e) {
            logger.error("Email verification failed", e);
            return null;
        }
    }
    
    // 비밀번호 재설정 관련 메서드들 추가
    public String requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        EmailVerificationEvent event = new EmailVerificationEvent(
          email,
          user.getName(),
          EmailVerificationEvent.EventType.PASSWORD_RESET
        );
        eventPublisher.publishEvent(event);
        
        return event.getJwtToken();
    }
    
    public boolean verifyPasswordResetCode(String email, String verificationCode, String jwtToken) {
        try {
            Claims claims = Jwts.parserBuilder()
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
        Claims claims = Jwts.parserBuilder()
          .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
          .build()
          .parseClaimsJws(jwtToken)
          .getBody();
        
        String email = claims.get("email", String.class);
        
        if (!email.equals(request.getEmail())) {
            throw new IllegalArgumentException("토큰의 이메일 정보가 일치하지 않습니다.");
        }
        
        User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        User updatedUser = user.toBuilder()
          .password(passwordEncoder.encode(request.getNewPassword()))
          .build();
        
        updatedUser.setCreatedAt(user.getCreatedAt());
        updatedUser.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(updatedUser);
    }
    
    @Transactional
    public void completeRegister(UserRegisterDto userRegisterDto, MultipartFile profileImage, String jwtToken) {
        try {
            Claims claims = validateRegistrationToken(jwtToken);
            String email = claims.get("email", String.class);
            Boolean isEmailVerified = claims.get("isEmailVerified", Boolean.class);
            
            if (!isEmailVerified) {
                throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
            }
            
            if (userRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("이미 가입된 이메일입니다.");
            }
            
            User newUser = User.builder()
              .email(email)
              .password(passwordEncoder.encode(userRegisterDto.getPassword()))
              .name(userRegisterDto.getName())
              .emailVerified(true)
              .loginAttempts(0)
              .accountLocked(false)
              .lastLoginAttempt(LocalDateTime.now())
              .status(UserStatus.ACTIVE)
              .build();
            
            if (profileImage != null && !profileImage.isEmpty()) {
                String imageUrl = s3Service.uploadProfileImage(profileImage, email);
                newUser = newUser.toBuilder()
                  .profileImgUrl(imageUrl)
                  .build();
            }
            
            User savedUser = userRepository.save(newUser);
            
            eventPublisher.publishEvent(new EmailVerificationEvent(
              email,
              savedUser.getName(),
              EmailVerificationEvent.EventType.WELCOME
            ));
        } catch (JwtException e) {
            throw new IllegalArgumentException("유효하지 않은 인증 토큰입니다.");
        }
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    public boolean isAccountLocked(String email) {
        User user = getUserByEmail(email);
        if (user == null) return false;
        
        if (user.isAccountLocked() && user.getLockedUntil() != null &&
          LocalDateTime.now().isAfter(user.getLockedUntil())) {
            unlockAccount(email);
            return false;
        }
        
        return user.isAccountLocked();
    }
    
    public int incrementLoginAttempts(String email) {
        return userRepository.findByEmail(email)
          .map(user -> {
              int newAttempts = user.getLoginAttempts() + 1;
              User updatedUser = user.toBuilder()
                .loginAttempts(newAttempts)
                .lastLoginAttempt(LocalDateTime.now())
                .accountLocked(newAttempts >= 5)
                .lockedUntil(newAttempts >= 5 ? LocalDateTime.now().plusMinutes(30) : null)
                .build();
              
              updatedUser.setCreatedAt(user.getCreatedAt());
              updatedUser.setUpdatedAt(LocalDateTime.now());
              
              userRepository.save(updatedUser);
              return newAttempts;
          })
          .orElse(0);
    }
    
    public void unlockAccount(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            User unlockedUser = user.toBuilder()
              .accountLocked(false)
              .loginAttempts(0)
              .lockedUntil(null)
              .build();
            
            unlockedUser.setCreatedAt(user.getCreatedAt());
            unlockedUser.setUpdatedAt(LocalDateTime.now());
            
            userRepository.save(unlockedUser);
        });
    }
    
    @Transactional
    public User updateUser(String email, String newPassword, String name, String profileImgUrl) {
        User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        User updatedUser = user.toBuilder()
          .password(newPassword != null ? passwordEncoder.encode(newPassword) : user.getPassword())
          .name(name != null ? name : user.getName())
          .profileImgUrl(profileImgUrl != null ? profileImgUrl : user.getProfileImgUrl())
          .build();
        
        updatedUser.setCreatedAt(user.getCreatedAt());
        updatedUser.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(updatedUser);
    }
    
    @Transactional
    public User updateProfileImage(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        try {
            if (user.getProfileImgUrl() != null && !user.getProfileImgUrl().isEmpty()) {
                s3Service.deleteImage(user.getProfileImgUrl());
            }
            
            String imageUrl = s3Service.uploadProfileImage(file, email);
            
            User updatedUser = user.toBuilder()
              .profileImgUrl(imageUrl)
              .build();
            
            updatedUser.setCreatedAt(user.getCreatedAt());
            updatedUser.setUpdatedAt(LocalDateTime.now());
            
            return userRepository.save(updatedUser);
        } catch (Exception e) {
            logger.error("프로필 이미지 업데이트 실패", e);
            throw new S3Exception("프로필 이미지 업데이트에 실패했습니다: " + e.getMessage());
        }
    }
    
    public void deactivateUser(String email) {
        User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        User updatedUser = user.toBuilder()
          .status(UserStatus.INACTIVE)
          .build();
        
        updatedUser.setCreatedAt(user.getCreatedAt());
        updatedUser.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(updatedUser);
        
        eventPublisher.publishEvent(new EmailVerificationEvent(
          email,
          user.getName(),
          EmailVerificationEvent.EventType.ACCOUNT_DEACTIVATED
        ));
    }
    
    public void activateUser(String email) {
        User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        if (user.getStatus() != UserStatus.INACTIVE) {
            throw new IllegalArgumentException("비활성화된 계정만 활성화할 수 있습니다.");
        }
        
        User updatedUser = user.toBuilder()
          .status(UserStatus.ACTIVE)
          .build();
        
        updatedUser.setCreatedAt(user.getCreatedAt());
        updatedUser.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(updatedUser);
        
        eventPublisher.publishEvent(new EmailVerificationEvent(
          email,
          user.getName(),
          EmailVerificationEvent.EventType.ACCOUNT_ACTIVATED
        ));
    }
    
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
    }
    
    private Claims validateRegistrationToken(String token) {
        return Jwts.parserBuilder()
          .setSigningKey(secretKey.getBytes())
          .build()
          .parseClaimsJws(token)
          .getBody();
    }
}