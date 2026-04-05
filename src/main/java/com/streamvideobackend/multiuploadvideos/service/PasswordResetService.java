package com.streamvideobackend.multiuploadvideos.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.streamvideobackend.multiuploadvideos.dto.PasswordResetToken;
import com.streamvideobackend.multiuploadvideos.dto.User;
import com.streamvideobackend.multiuploadvideos.repository.PasswordRepository;
import com.streamvideobackend.multiuploadvideos.repository.UserRepository;

@Service
public class PasswordResetService {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    public PasswordResetService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // 🔹 Process forgot password
    public void processForgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        // Silent return if email doesn't exist
        if (userOptional.isEmpty()) return;

        User user = userOptional.get();

        // Check if a valid token already exists
        Optional<PasswordResetToken> existingTokenOpt = tokenRepository
                .findByUserAndExpiryDateAfter(user, LocalDateTime.now());

        if (existingTokenOpt.isPresent()) {
            // Token exists and is still valid
            // ✅ Do NOT send email again
            return; // just silently return
        }

        // Generate a new token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        tokenRepository.save(resetToken);

        String resetLink = "http://localhost:3000/reset-password?token=" + token;

        // Send email only if new token is created
        emailService.sendResetEmail(user.getEmail(), resetLink);
    }

    // 🔹 Reset password using token
    public void resetPassword(String token, String newPassword) {

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Invalid token");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Token expired");
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete token after use
        tokenRepository.delete(resetToken);
    }
}

    
    



