package com.min.i.memory_BE.domain.user.service;

import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.UserMailStatus;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository; // UserRepository를 사용해 이메일 인증 코드 및 유효 기간을 저장


    @Value("${spring.mail.username}")
    private String fromEmail;


    // 이메일 인증 코드 발송
    public boolean sendVerificationCode(UserRegisterDto userRegisterDto) {
        try { // 인증 코드 생성 (6자리 숫자)
            String verificationCode = generateVerificationCode();

            // 인증 코드와 유효 기간 설정
            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(15); // 15분 유효

            // 이메일로 사용자 검색 (기존 사용자 확인)
            User user = userRepository.findByEmail(userRegisterDto.getEmail())
                    .orElse(null); // 임시 사용자가 없다면 null 반환

            if (user != null) {
                // 기존 사용자가 있을 경우, 등록된 사용자인지 확인
                if (user.getMailStatus() == UserMailStatus.REGISTERED) {
                    throw new IllegalArgumentException("이메일은 이미 가입이 완료되었습니다.");
                }

                // 미인증 사용자라면 삭제하고 새로 임시 사용자로 저장
                if (user.getMailStatus() == UserMailStatus.UNVERIFIED) {
                    userRepository.delete(user); // 기존 미인증 사용자 삭제
                }
            }

            // 새로 임시 사용자로 저장 (이메일 인증을 위해 임시로 데이터 저장)
            user = User.createTemporaryUser(userRegisterDto.getEmail(), verificationCode, expirationTime);

            // 데이터베이스에 임시 사용자 저장
            userRepository.save(user);

            // 메일 내용 설정
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);  // 발신자 이메일
            message.setTo(userRegisterDto.getEmail());
            message.setSubject("이메일 인증 코드");
            message.setText("인증 코드: " + verificationCode);

            // 메일 전송
            mailSender.send(message);

            return true; // 이메일 전송 성공

        } catch (Exception e) {
            // 다른 예외 처리
            System.err.println("알 수 없는 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();  // 예외의 StackTrace 출력
            return false; // 기타 예외 발생 시 false 반환
        }

    }

    // 인증 코드 생성
    private String generateVerificationCode() {
        return String.valueOf((int)(Math.random() * 1000000));  // 6자리 랜덤 숫자
    }

}

