package com.example.model;

import lombok.Data;

import java.security.SecureRandom;
import java.time.Instant;

@Data
public class OtpData {
    private final int otp;
    private final Instant expirationTime;
    private boolean isVerified = false; // is set to true when validate method is called
    private static final int VALID_FOR = 30 * 60; // seconds
    // safer than random
    private final SecureRandom generator = new SecureRandom();

    public OtpData() {
        this.otp = generator.nextInt(100000, 1000000); // six digits
        this.expirationTime = Instant.now().plusSeconds(VALID_FOR);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expirationTime);
    }
}

