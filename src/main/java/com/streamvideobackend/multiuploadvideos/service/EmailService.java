package com.streamvideobackend.multiuploadvideos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendTestEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("sayandatta9876@gmail.com"); // can be anything
        message.setSubject("Test Email");
        message.setText("Hello from Spring Boot 🚀");
        message.setFrom("sayandatta9876@gmail.com");

        mailSender.send(message);
    }
    
    public void sendResetEmail(String to, String link) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Request");
        message.setText("Click the link to reset your password:\n" + link);
        message.setFrom("sayandatta9876@gmail.com");

        mailSender.send(message);
    }
    
    public void sendOtpEmail(String to, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Email Verification OTP");
        message.setText("Your verification OTP is: " + otp +
                "\nThis OTP is valid for 10 minutes." + "\nGo to the link below"+"\nhttps://video-streaming-frontend-eight.vercel.app/verify-account");
        message.setFrom("sayandatta9876@gmail.com");

        mailSender.send(message);
    }

}

