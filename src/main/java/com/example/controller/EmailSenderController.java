package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.service.EmailSenderService;
import com.example.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor

public class EmailSenderController {
    private final EmailSenderService emailSenderService;
    private final OtpService otpService;

    @PostMapping()
    public ResponseEntity<ApiResponse<Object>> sendEmail(@RequestBody Map<String,String> emailMap) {
        String email = emailMap.get("email");
        return otpService.sentOtp(email);
    }
}
