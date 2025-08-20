package com.example.controller;

import com.example.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class HealthCheckController {
    @RequestMapping("/ping")
    public ResponseEntity<ApiResponse<Object>> healthCheck() {
        return ApiResponse.buildResponse(HttpStatus.OK, true, "pong",null);
    }
}
