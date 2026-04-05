package com.streamvideobackend.multiuploadvideos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.streamvideobackend.multiuploadvideos.dto.EmailVerificationOTP;
import com.streamvideobackend.multiuploadvideos.dto.User;

@Repository
public interface EmailVerificationOTPRepository 
        extends JpaRepository<EmailVerificationOTP, Long> {

    Optional<EmailVerificationOTP> findByUser(User user);
}

