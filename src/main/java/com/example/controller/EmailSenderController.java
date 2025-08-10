package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.service.EmailSenderService;
import com.example.service.OtpService;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EmailSenderController {
    private final OtpService otpService;
    private final UserService userService;

    @PostMapping("/")
    public ResponseEntity<ApiResponse<Object>> sendEmail(@RequestBody Map<String,String> emailMap) {
        String email = emailMap.get("email");
        return otpService.sentOtp(email);
    }
    @PostMapping("/verifyOtp")
    public ResponseEntity<ApiResponse<Object>> verifyOtp(@RequestBody Map<String, String> otpRequest) {
        return otpService.validateOtp(otpRequest.get("email"), otpRequest.get("otp"));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@RequestBody Map<String, String> resetPasswordRequest) {
        return userService.resetPassword(resetPasswordRequest.get("otp"),resetPasswordRequest.get("email"), resetPasswordRequest.get("password"));
    }


}
