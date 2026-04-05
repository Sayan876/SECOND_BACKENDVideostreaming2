package com.streamvideobackend.multiuploadvideos.repository;

import java.time.LocalDateTime;
import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.streamvideobackend.multiuploadvideos.dto.PasswordResetToken;
import com.streamvideobackend.multiuploadvideos.dto.User;

@Repository
public interface PasswordRepository 
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);
    
    Optional<PasswordResetToken> findByUserAndExpiryDateAfter(User user, LocalDateTime now);

    void deleteByUser(User user);

}

