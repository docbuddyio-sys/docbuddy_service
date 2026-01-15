package com.example.lifevault.service;

import com.example.lifevault.entity.User;
import com.example.lifevault.exception.UnauthorizedException;
import com.example.lifevault.repository.UserRepository;
import com.example.lifevault.security.JwtUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtUtils jwtUtils;

    public Map<String,String> googleLoginService(String email){

            User appUser = userRepository.findByEmail(email).orElseThrow(()-> new UnauthorizedException("User Not found!!"));
            String jwt  =jwtUtils.generateJwtTokenFromUsername(email) ;
            Map<String, String> claim = Map.of(
                    "access_token", jwt,
                    "user_id", String.valueOf(appUser.getId())
            );


            return claim;

    }

}
