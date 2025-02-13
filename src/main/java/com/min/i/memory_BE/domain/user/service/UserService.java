package com.min.i.memory_BE.domain.user.service;

import com.min.i.memory_BE.domain.user.dto.JwtAuthenticationResponse;
import com.min.i.memory_BE.domain.user.dto.PasswordResetDto;
import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.dto.UserUpdateDto;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.UserStatus;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import com.min.i.memory_BE.domain.user.event.EmailVerificationEvent;
import com.min.i.memory_BE.global.security.jwt.JwtTokenProvider;
import com.min.i.memory_BE.global.service.S3Service;
import io.jsonwebtoken.Claims;
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
    
    // 인증 관련 메서드
    public JwtAuthenticationResponse generateTokens(String email) {
        String token = jwtTokenProvider.generateToken(email);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);
        return new JwtAuthenticationResponse(token, refreshToken);
    }

    // 이메일 인증 관련 메서드
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
                  .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS512)
                        .compact();
            }
            return null;
        } catch (Exception e) {
            logger.error("Email verification failed", e);
            return null;
        }
    }
    
    // 회원가입 완료
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
    
    // 사용자 조회 및 계정 잠금 관련 메서드
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public boolean isAccountLocked(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return false;
        }

        if (user.isAccountLocked() && user.getLockedUntil() != null && LocalDateTime.now().isAfter(user.getLockedUntil())) {
            unlockAccount(email);
            return false;
        }

        return user.isAccountLocked();
    }

    public int incrementLoginAttempts(String email) {
        return userRepository.findByEmail(email)
          .map(existingUser -> {
              int newAttempts = existingUser.getLoginAttempts() + 1;
              
              User updatedUser = existingUser.toBuilder()
                .id(existingUser.getId())
                .loginAttempts(newAttempts)
                .lastLoginAttempt(LocalDateTime.now())
                .accountLocked(newAttempts >= 5)
                .lockedUntil(newAttempts >= 5 ? LocalDateTime.now().plusMinutes(30) : existingUser.getLockedUntil())
                .build();

              updatedUser.setCreatedAt(existingUser.getCreatedAt());
              updatedUser.setUpdatedAt(LocalDateTime.now());
              
              userRepository.save(updatedUser);
              return newAttempts;
          })
          .orElse(0);
    }
    
    public void unlockAccount(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            User unlockedUser = user.toBuilder()
              .accountLocked(false)
              .loginAttempts(0)
              .lockedUntil(null)
              .build();
            
            unlockedUser.setCreatedAt(user.getCreatedAt());
            unlockedUser.setUpdatedAt(LocalDateTime.now());
            
            userRepository.save(unlockedUser);
        }
    }
    
    // 사용자 정보 수정 관련 메서드
    @Transactional
    public User updateUser(String email, UserUpdateDto updateDto, MultipartFile profileImage) {
        User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        if (updateDto.getNewPassword() != null &&
          passwordEncoder.matches(updateDto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }
        
        String profileImgUrl = user.getProfileImgUrl();
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImgUrl = s3Service.updateProfileImage(
              profileImage,
              String.valueOf(user.getId()),
              user.getProfileImgUrl()
            );
        }
        
        User updatedUser = user.toBuilder()
          .password(updateDto.getNewPassword() != null ?
            passwordEncoder.encode(updateDto.getNewPassword()) :
            user.getPassword())
          .name(updateDto.getName() != null ?
            updateDto.getName() :
            user.getName())
          .profileImgUrl(profileImgUrl)
                    .build();

        updatedUser.setCreatedAt(user.getCreatedAt());
        updatedUser.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(updatedUser);
    }
    
    // 비밀번호 재설정 관련 메서드
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
    
    // 계정 상태 관리 메서드
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
    
    // 유틸리티 메서드
    private Claims validateRegistrationToken(String token) {
        return Jwts.parserBuilder()
          .setSigningKey(secretKey.getBytes())
          .build()
          .parseClaimsJws(token)
          .getBody();
    }
}