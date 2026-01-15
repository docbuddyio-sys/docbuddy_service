package com.example.lifevault.controller;

import com.example.lifevault.entity.User;
import com.example.lifevault.exception.UnauthorizedException;
import com.example.lifevault.repository.UserRepository;
import com.example.lifevault.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import com.example.lifevault.payload.*;
import com.example.lifevault.service.AuthService;
import com.example.lifevault.service.GoogleAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthService authService;

    @Autowired
    private GoogleAuthService googleAuthService;
    @Autowired
    UserService userService;

    // Request DTO for the mobile client's token
    public static class IdTokenRequest {
        public String idToken;
    }

    // Response DTO for sending back your application's token
    public static class AuthResponse {
        public String appToken;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return ResponseEntity.ok(authService.signup(signUpRequest));
    }

    @PostMapping("/google-signup")
    public ResponseEntity<?> googleSignup(@RequestBody IdTokenRequest request) {
        Payload payload = googleAuthService.verifyToken(request.idToken);

        if (payload == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid Google ID Token."));
        }

        try {
           // System.out.println("Full Payload: " + payload.toPrettyString());

            // Extract user information
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String googleUserId = payload.getSubject();
//
//            System.out.println("Email: " + email);
//            System.out.println("Name: " + name);
//            System.out.println("Google User ID: " + googleUserId);

            if (email == null) {
                return ResponseEntity.status(400).body(Map.of("message",
                        "Google ID Token is valid but does not contain an email. Please ensure your client requests the 'email' scope and the user has granted permission."));
            }

            // 1. CHECK/CREATE USER IN YOUR DATABASE
            // This is where you implement your logic:
            // - Look up user by email or googleUserId.
            // - If user doesn't exist, register them (Sign-up).
            // - If user exists, log them in (Sign-in).
//            if (userRepository.existsByEmail(email)) {
//                throw new Exception("hello ");
//            }

            // 2. GENERATE YOUR APPLICATION'S JWT
            // This custom token is what your app will use for subsequent API calls.
            return ResponseEntity.ok(authService.signupGuser(email, name, googleUserId));

        } catch (Exception e) {
            // Handle database, JWT creation, or other internal server errors
            System.err.println("Server error during Google auth process: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Internal server error during authentication."));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerificationRequest otpVerificationRequest) {
        return ResponseEntity.ok(authService.verifyOtp(otpVerificationRequest));
    }

    @PostMapping("/setup-mpin")
    public ResponseEntity<?> setupMpin(@Valid @RequestBody MpinSetupRequest mpinSetupRequest) {
        return ResponseEntity.ok(authService.setupMpin(mpinSetupRequest));
    }

    @PostMapping("/mpin-login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/g-login")
    public ResponseEntity<Map<String,String>> googleLogin(@Valid @RequestBody IdTokenRequest request ){
        Payload payload = googleAuthService.verifyToken(request.idToken);

        if (payload == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid Google ID Token."));
        }

            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String googleUserId = payload.getSubject();
            return  ResponseEntity.ok(userService.googleLoginService(email));


    }

}
