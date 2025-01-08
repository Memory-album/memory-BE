package com.min.i.memory_BE.domain.user.service;

import com.min.i.memory_BE.domain.user.dto.JwtAuthenticationResponse;
import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.dto.UserRegisterResultDto;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.UserStatus;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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

        // 최종 사용자로 build (이메일, 암호화된 비밀번호, 이름, 프로필 이미지 등)
        User newUser = User.builder()
                .email(email)  // JWT에서 가져온 이메일 사용
                .password(hashedPassword)  // 암호화된 비밀번호
                .name(userRegisterDto.getName())  // 사용자가 입력한 이름
                .profileImgUrl(userRegisterDto.getProfileImgUrl())  // 사용자가 입력한 프로필 이미지 URL
                .emailVerified(true)  // 이메일 인증 완료
                .loginAttempts(0)  // 로그인 시도 횟수 초기화 (기본값 0이지만 명시적으로 설정 가능)
                .accountLocked(false)  // 계정 잠금 초기화
                .lastLoginAttempt(LocalDateTime.now())  // 마지막 로그인 시도 시간 현재 시간으로 설정
                .lockedUntil(null)  // 잠금 해제 시간 초기화 (null로 설정)
                .status(UserStatus.ACTIVE) // 사용자 상태 기본값 = 활성화
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
        User user = userRepository.findByEmail(email).orElse(null);

        // 이메일이 존재하지 않으면 로그인 실패 처리를 하고, 새로운 사용자 추가는 하지 않음
        if (user == null) {
            return 0;  // 이메일이 존재하지 않으면 로그인 실패 처리만 하고, 시도 횟수 증가하지 않음
        }

        // 로그인 시도 횟수 증가 및 로그인 시도 시간 갱신
        user = user.toBuilder()
                .loginAttempts(user.getLoginAttempts() + 1)
                .lastLoginAttempt(LocalDateTime.now())  // 로그인 시도 시간 갱신
                .build();

            // 로그인 시도 횟수가 5번 이상이면 계정 잠금
            if (user.getLoginAttempts() >= 5) {
                lockAccount(user);
            } else {
                userRepository.save(user);  // 로그인 시도 횟수 증가 후 저장
            }

            return user.getLoginAttempts();  // 로그인 시도 횟수 반환

    }

    // 계정을 잠그는 메서드
    private void lockAccount(User user) {
        // 기존 값을 기반으로 새 User 객체 생성하여 계정 잠금 처리
        User lockedUser = user.toBuilder()
                .accountLocked(true)
                .lockedUntil(LocalDateTime.now().plusMinutes(30))  // 30분 후 잠금 해제
                .build();

        userRepository.save(lockedUser);  // 계정 잠금 상태 저장

        // 서버 로그로 계정 잠금 정보 출력
        System.out.println("계정이 잠겼습니다. 30분 동안 다시 시도할 수 없습니다.");
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

            userRepository.save(unlockedUser);  // 계정 잠금 해제
        }
    }

    // 사용자 정보 수정
    public User updateUser(String email, String password, String name, String profileImgUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 기존 사용자 정보를 바탕으로 변경할 부분만 수정하여 새로운 User 객체를 생성
        User updatedUser = user.toBuilder()
                .password(password != null ? password : user.getPassword())  // 비밀번호 수정
                .name(name != null ? name : user.getName())  // 이름 수정
                .profileImgUrl(profileImgUrl != null ? profileImgUrl : user.getProfileImgUrl())  // 프로필 사진 수정
                .build();  // toBuilder로 새로운 객체 생성

        return userRepository.save(updatedUser);  // 수정된 사용자 정보 저장
    }

    // 사용자 비활성화
    public void deactivateUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 상태를 '비활성'으로 변경
        User updatedUser = user.toBuilder()
                .status(UserStatus.INACTIVE)  // 사용자 상태를 '비활성'으로 변경
                .build();

        //비활성화 상태로 변경된 사용자 정보 저장
        userRepository.save(updatedUser);
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
                .status(UserStatus.ACTIVE)  // 상태를 'ACTIVE'로 변경
                .build();

        userRepository.save(updatedUser);
    }

    // 사용자 탈퇴 (계정 영구 삭제)
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        userRepository.delete(user);  // 사용자 계정 삭제
    }
}
