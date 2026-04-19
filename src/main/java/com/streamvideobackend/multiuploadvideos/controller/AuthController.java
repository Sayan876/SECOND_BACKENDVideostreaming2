package com.streamvideobackend.multiuploadvideos.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.streamvideobackend.multiuploadvideos.config.JwtUtil;
import com.streamvideobackend.multiuploadvideos.config.SecurityConfig;
import com.streamvideobackend.multiuploadvideos.dto.InvalidCredentialsException;
import com.streamvideobackend.multiuploadvideos.dto.User;
import com.streamvideobackend.multiuploadvideos.repository.UserRepository;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;



@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://video-streaming-frontend-eight.vercel.app", allowedHeaders = "*")
public class AuthController {
	
	private final UserRepository userRepository; 
	
	private final PasswordEncoder passwordEncoder;  
	
	private final JwtUtil jwtUtil; 
	
	@PostMapping("/login")
	public String login(@RequestBody User user) {

	    User dbUser = userRepository.findByEmail(user.getEmail())
	            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

	    if (!passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
	    	throw new InvalidCredentialsException("Invalid email or password");
	    }

	    return jwtUtil.generateToken(dbUser.getEmail());
	}
	
	
	

}
