package com.example.lifevault.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleAuthService {

    // Your Web Client ID from Google Cloud Console
    @Value("${google.web-client-id}")
    private String webClientId;

    private GoogleIdTokenVerifier verifier;

    @jakarta.annotation.PostConstruct
    public void init() {
        // Initialize the verifier only once, after webClientId is injected
        System.out.println("Initializing GoogleAuthService with Web Client ID: " + webClientId);
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                // Replace with your actual Web Client ID list
                .setAudience(Collections.singletonList(webClientId))
                .build();
    }

    public GoogleAuthService() {
        // Default constructor
    }

    /**
     * Verifies the Google ID Token and returns the token payload if valid.
     * 
     * @param idTokenString The JWT string received from the React Native client.
     * @return The token's payload containing user details, or null if invalid.
     */
    public Payload verifyToken(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            System.out.println("Token verification in process for: " + idTokenString);

            if (idToken != null) {
                Payload payload = idToken.getPayload();

                // IMPORTANT: Check that the token was signed for your app (audience check is in
                // the verifier, but check issuer)
                String issuer = payload.getIssuer();
                if (!"accounts.google.com".equals(issuer) && !"https://accounts.google.com".equals(issuer)) {
                    System.err.println("Invalid Issuer: " + issuer);
                    throw new GeneralSecurityException("Token issuer is invalid.");
                }

                return payload;
            } else {
                // Token is invalid (e.g., wrong signature, expired, or invalid audience)
                System.err.println("Token verification failed: ID Token is null after verify.");

                // Debugging: Parse without verification to see what's wrong
                try {
                    GoogleIdToken parsedToken = GoogleIdToken.parse(new GsonFactory(), idTokenString);
                    if (parsedToken != null) {
                        Payload debugPayload = parsedToken.getPayload();
                        System.err.println("Debug - Token Audience: " + debugPayload.getAudience());
                        System.err.println("Debug - Configured Client ID: " + webClientId);
                        System.err.println("Debug - Token Issuer: " + debugPayload.getIssuer());
                        System.err.println("Debug - Token Expiry: " + debugPayload.getExpirationTimeSeconds());
                    }
                } catch (IOException ex) {
                    System.err.println("Debug - Could not parse token for debugging: " + ex.getMessage());
                }

                return null;
            }
        } catch (GeneralSecurityException | IOException e) {
            // Log the error (e.g., signature verification failed, I/O error retrieving
            // keys)
            System.err.println("Token verification failed with Exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
