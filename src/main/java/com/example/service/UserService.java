package com.example.service;

import com.example.dto.ApiResponse;
import com.example.model.User;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public ResponseEntity<ApiResponse<Object>> resetPassword(String otp, String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ApiResponse.buildResponse(HttpStatus.BAD_REQUEST, false, "User Does Not Exist", null);
        }
        ResponseEntity<ApiResponse<Object>> response = otpService.validateOtp(otp, email); // validate again to ensure security
        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        user.setPassword(encoder.encode(password));
        userRepository.save(user);
        otpService.clearOtp(email);
        return ApiResponse.buildResponse(HttpStatus.OK, true, "Password Reset Successfully", null);
    }
}


