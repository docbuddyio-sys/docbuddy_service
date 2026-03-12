package com.example.lifevault.service;

import com.example.lifevault.entity.Otp;
import com.example.lifevault.entity.User;
import com.example.lifevault.payload.*;
import com.example.lifevault.repository.OtpRepository;
import com.example.lifevault.repository.UserDeviceRepository;
import com.example.lifevault.repository.UserRepository;
import com.example.lifevault.security.JwtUtils;
import com.example.lifevault.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    OtpRepository otpRepository;

    @Autowired
    UserDeviceRepository userDeviceRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    public MessageResponse signup(SignupRequest request) {
        if (userRepository.existsByMobile(request.getMobile())) {
            throw new RuntimeException("Error: Mobile is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create user but not verified yet
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setRoles("USER");
        // user.setVerified(false);

        userRepository.save(user);

        // Generate OTP
        String otpCode = String.format("%04d", new Random().nextInt(10000));
        Otp otp = new Otp();
        otp.setMobile(request.getMobile());
        otp.setOtpCode(otpCode);
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5)); // 5 mins expiry

        otpRepository.save(otp);

        return new MessageResponse("User registered successfully! OTP: " + otpCode);
    }

    public MessageResponse verifyOtp(OtpVerificationRequest request) {
        Otp otp = otpRepository.findByMobile(request.getMobile())
                .orElseThrow(() -> new RuntimeException("Error: OTP not found!"));

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otp);
            throw new RuntimeException("Error: OTP expired!");
        }

        if (!otp.getOtpCode().equals(request.getOtp())) {
            throw new RuntimeException("Error: Invalid OTP!");
        }

        // Verify User
        User user = userRepository.findByMobile(request.getMobile())
                .orElseThrow(() -> new RuntimeException("Error: User not found!"));
        // user.setVerified(true);
        userRepository.save(user);

        // Delete OTP after successful verification
        otpRepository.delete(otp);

        return new MessageResponse("OTP Verified Successfully!");
    }

    public JwtResponse setupMpin(MpinSetupRequest request, String jwtToken) {
        // Extract user email from JWT token
        String email = jwtUtils.getUserNameFromJwtToken(jwtToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Error: User not found!"));

        // Set MPIN only if not already set (one MPIN per user)
        if (user.getMpin() == null || user.getMpin().isEmpty()) {
            user.setMpin(encoder.encode(request.getMpin()));
            userRepository.save(user);
        }

        // Register the device if not already registered
        if (!userDeviceRepository.existsByUsrAndDeviceId(user, request.getDeviceId())) {
            com.example.lifevault.entity.UserDevice userDevice = new com.example.lifevault.entity.UserDevice();
            userDevice.setUsr(user);
            userDevice.setDeviceId(request.getDeviceId());
            userDevice.setCreatedAt(new java.util.Date());
            userDevice.setUpdatedAt(new java.util.Date());
            userDevice.setStatus("ACTIVE");
            userDeviceRepository.save(userDevice);
        }

        // Generate JWT token for immediate login
        String jwt = jwtUtils.generateJwtTokenFromUsername(user.getEmail());

        return new JwtResponse(jwt,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobile(),
                user.getRoles());
    }

    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getMobile(), request.getMpin()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();

        return new JwtResponse(jwt,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobile(),
                user.getRoles());
    }

    public Object signupGuser(String email, String name, String googleId) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setRoles("USER");
        String jwt = jwtUtils.generateJwtTokenFromUsername(user.getEmail());
        userRepository.save(user);

        return new JwtResponse(jwt,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobile(),
                user.getRoles());
    }

    public JwtResponse mpinLogin(MpinLoginRequest request) {
        // Find device by device ID
        com.example.lifevault.entity.UserDevice userDevice = userDeviceRepository.findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Error: Device not registered!"));

        User user = userDevice.getUsr();

        // Validate MPIN
        if (user.getMpin() == null || !encoder.matches(request.getMpin(), user.getMpin())) {
            throw new RuntimeException("Error: Invalid MPIN!");
        }

        // Generate JWT token
        String jwt = jwtUtils.generateJwtTokenFromUsername(user.getEmail());

        return new JwtResponse(jwt,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobile(),
                user.getRoles());
    }
}
