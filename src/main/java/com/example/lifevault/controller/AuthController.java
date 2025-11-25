package com.example.lifevault.controller;

import com.example.lifevault.payload.*;
import com.example.lifevault.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return ResponseEntity.ok(authService.signup(signUpRequest));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerificationRequest otpVerificationRequest) {
        return ResponseEntity.ok(authService.verifyOtp(otpVerificationRequest));
    }

    @PostMapping("/setup-mpin")
    public ResponseEntity<?> setupMpin(@Valid @RequestBody MpinSetupRequest mpinSetupRequest) {
        return ResponseEntity.ok(authService.setupMpin(mpinSetupRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}
