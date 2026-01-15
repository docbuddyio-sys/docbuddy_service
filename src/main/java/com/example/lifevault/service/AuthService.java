package com.example.lifevault.service;

import com.example.lifevault.entity.Otp;
import com.example.lifevault.entity.User;
import com.example.lifevault.payload.*;
import com.example.lifevault.repository.OtpRepository;
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
    PasswordEncoder encoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    public MessageResponse signup(SignupRequest request) {
        if (userRepository.existsByMobile(request.getMobile())) {
            return new MessageResponse("Error: Mobile is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return new MessageResponse("Error: Email is already in use!");
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
            return new MessageResponse("Error: OTP expired!");
        }

        if (!otp.getOtpCode().equals(request.getOtp())) {
            return new MessageResponse("Error: Invalid OTP!");
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

    public JwtResponse setupMpin(MpinSetupRequest request) {
//        User user = userRepository.findByMobile(request.getDeviceId())
//                .orElseThrow(() -> new RuntimeException("Error: User not found!!!!!!"));

        // if (!user.isVerified()) {
        // throw new RuntimeException("Error: User not verified!");
        // }

        user.setMpin(encoder.encode(request.getMpin()));
        userRepository.save(user);

        // Auto login
        return login(new LoginRequest(request.getDeviceId(), request.getMpin())); // Reusing login logic logic slightly
                                                                                // modified
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
            return new MessageResponse("Error: Email is already in use!");
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
}
