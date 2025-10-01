package com.bidnbuy.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    //2차 인증 코드 생성
    public String createVerificationCode(){
        //랜덤 6 자리 숫자 생성하기
        int code = (int)(Math.random()*899999)+ 100000;
        return String.valueOf(code);
    }

    //이메일 전송하기
    public void sendVerificationEmail(String toEmail, String subject, String text){
        SimpleMailMessage message = new SimpleMailMessage();

        //application.properties에 설정한 username이 발신자
        message.setFrom(fromEmail);
        message.setTo(toEmail); //수신자
        message.setSubject(subject); //제목
        message.setText(text);//본문 내용

        mailSender.send(message);
    }

    //임시 비밀번호 이메일 발송
    public void sendTempPasswordEmail(String toEmail, String tempPassword){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[BID-n-BUY] 비밀번호 재설정 임시 비밀번호");
        String text = String.format(
            "요청하신 임시 비밀번호는 **%s** 입니다.\n\n" +
            "임시 비밀번호는 10분간 유효합니다.", tempPassword
        );
        message.setText(text);
        mailSender.send(message);
    }
}
