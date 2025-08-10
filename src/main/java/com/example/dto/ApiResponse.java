package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private String message;
    boolean success;
    Instant timeStamp;
    private T data;

    public ApiResponse(boolean success, String message, T data) {
        this.timeStamp = Instant.now();
        this.message = message;
        this.success = success;
        this.data = data;
    }

    public static <T> ResponseEntity<ApiResponse<T>> buildResponse(HttpStatus status, boolean success, String message, T data) {
        return ResponseEntity.status(status).body(new ApiResponse<>(success, message, data));
    }
}
