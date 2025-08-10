package com.example.service;

import com.example.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSenderService {
    private final JavaMailSender mailSender;

    @Async
    public void sendEmail(String email, int otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Reset Password");
            message.setText("Dear user,\n" +
                    "This is your OTP to reset your password\n" +
                    "\t" + otp + "\n" +
                    "if you have not requested a password reset ignore this email");
            mailSender.send(message);
            System.out.println("Email sent to: " + email);
        } catch (Exception e) {
            System.out.println("Failed to send email to " + email + ": " + e.getMessage());
        }
    }
}
