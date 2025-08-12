package com.example.service;

import com.example.dto.ApiResponse;
import com.example.model.OtpData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OtpService {
    // store otps in a thread-safe map where the key is user email
    private final ConcurrentHashMap<String, OtpData> map = new ConcurrentHashMap<>();
    private final EmailSenderService emailSenderService;

    public int generateOtp(String email) {
        OtpData otpData = new OtpData();
        map.put(email, otpData);
        return otpData.getOtp();
    }

    public ResponseEntity<ApiResponse<Object>> validateOtp(String email, String otpString) {
        int otp;
        try {
            otp = Integer.parseInt(otpString);
            if (otp < 100000 || otp > 999999)
                return ApiResponse.buildResponse(HttpStatus.BAD_REQUEST, false, "OTP must be 6 digits", null);
        } catch (NumberFormatException e) {
            return ApiResponse.buildResponse(HttpStatus.BAD_REQUEST, false, "OTP must be a number", null);
        }
        OtpData otpData = map.get(email);
        if (otpData == null)
            return ApiResponse.buildResponse(HttpStatus.BAD_REQUEST, false, "No OTP exists for this email", null);
        if (otpData.isExpired())
            return ApiResponse.buildResponse(HttpStatus.BAD_REQUEST, false, "OTP expired", null);
        if (otpData.getOtp() != otp)
            return ApiResponse.buildResponse(HttpStatus.BAD_REQUEST, false, "OTP is invalid", null);
        otpData.setVerified(true);
        System.out.println(map.get(email).toString());
        return ApiResponse.buildResponse(HttpStatus.OK, true, "OTP is valid", null);
    }

    public void clearOtp(String email) {
        map.remove(email);
    }

    public boolean otpExists(String email) {
        return map.containsKey(email);
    }

    public boolean isOtpVerified(String email) {
        return map.get(email).isVerified();
    }

    public ResponseEntity<ApiResponse<Object>> sentOtp(String email) {
        int otp = generateOtp(email);
        emailSenderService.sendEmail(email, otp); // fire and forget
        return ApiResponse.buildResponse(HttpStatus.OK, true, "email send initiated", null);
    }
}

