package com.streamvideobackend.multiuploadvideos.service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import java.util.Random;


import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.streamvideobackend.multiuploadvideos.dto.EmailVerificationOTP;
import com.streamvideobackend.multiuploadvideos.dto.User;
import com.streamvideobackend.multiuploadvideos.dto.Video;
import com.streamvideobackend.multiuploadvideos.exception.ApiException;
import com.streamvideobackend.multiuploadvideos.repository.EmailVerificationOTPRepository;
import com.streamvideobackend.multiuploadvideos.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final VideoService videoService;
	private final PasswordEncoder passwordEncoder;
	private final EmailVerificationOTPRepository otpRepository;
	private final EmailService emailService;



	private Cloudinary cloudinary;

	// Cloudinary credentials (same as in VideoService)
	@Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

	@PostConstruct
	public void initCloudinary() {
		cloudinary = new Cloudinary(
				ObjectUtils.asMap("cloud_name", cloudName, "api_key", apiKey, "api_secret", apiSecret));
	}
	
	private String generateOneName(String name) {
	    String cleanName = name.toLowerCase().replaceAll("[^a-z0-9]", "");
	    int random = 1000 + new java.util.Random().nextInt(90000);
	    return cleanName + "_" + random;
	}
	
	private String generateUniqueOneName(String name) {
	    String oneName;

	    do {
	        oneName = generateOneName(name);
	    } while (userRepository.existsByOneName(oneName));

	    return oneName;
	}
	
	
	public void backfillOneNamesForExistingUsers() {

	    List<User> users = userRepository.findAll();

	    for (User user : users) {

	        if (user.getOneName() == null || user.getOneName().isEmpty()) {

	            String oneName = generateUniqueOneName(user.getName());
	            user.setOneName(oneName);
	        }
	    }

	    userRepository.saveAll(users);
	}

	// Create user with profile pic uploaded to Cloudinary
	public User postUser(String name, String email, String password, String biodetails, String country,
			MultipartFile profilePic) {
		
		//First check email if it's already available in database. 
		if (userRepository.existsByEmail(email)) {
	        throw new RuntimeException("Email already exists");
	    }

		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(password));

		user.setBiodetails(biodetails);
		user.setCountry(country);
		
		user.setOneName(generateUniqueOneName(name));

		if (profilePic != null && !profilePic.isEmpty()) {
		    try {
		        Map uploadResult = cloudinary.uploader().upload(profilePic.getBytes(),
		                                ObjectUtils.asMap(
		                                    "folder", "users/profile_pics"
		                                ));
		        user.setProfilePicUrl(uploadResult.get("secure_url").toString());
		        user.setProfilePicPublicId(uploadResult.get("public_id").toString());
		        System.out.println("Profile picture uploaded: " + user.getProfilePicUrl());
		    } catch (Exception e) {
		        e.printStackTrace(); // log the real error
		        throw new RuntimeException("Failed to upload profile picture", e);
		    }
		}


		return userRepository.save(user);
	}

	// Update user profile image
	public User updateUserProfileImage(String email, MultipartFile newProfilePic) {

	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    try {
	        // delete old image
	        if (user.getProfilePicPublicId() != null) {
	            cloudinary.uploader().destroy(
	                    user.getProfilePicPublicId(),
	                    ObjectUtils.asMap("resource_type", "image")
	            );
	        }

	        // upload new image
	        Map uploadResult = cloudinary.uploader().upload(
	                newProfilePic.getBytes(),
	                ObjectUtils.asMap("resource_type", "image", "folder", "profilePics")
	        );

	        user.setProfilePicUrl(uploadResult.get("secure_url").toString());
	        user.setProfilePicPublicId(uploadResult.get("public_id").toString());

	        return userRepository.save(user);

	    } catch (IOException e) {
	        throw new RuntimeException("Upload failed", e);
	    }
	}

	//Update user details 
	public User updateDataUser(String email, String name, String biodetails, String country) {

	    Optional<User> optionalUser = userRepository.findByEmail(email);

	    if (optionalUser.isEmpty()) {
	        throw new RuntimeException("User not found");
	    }

	    User user = optionalUser.get();

	    user.setName(name);
	    user.setBiodetails(biodetails);
	    user.setCountry(country);

	    return userRepository.save(user);
	}


	//Change password 
	public User updateUserPassword(String newPassword, String email, String oldPassword) {

	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    // ✅ check old password
	    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
	        throw new RuntimeException("Old password is incorrect");
	    }

	    // ✅ set new password
	    user.setPassword(passwordEncoder.encode(newPassword));

	    return userRepository.save(user);
	}
	
	public Optional<User> getMyProfile(String email){
		return userRepository.findByEmail(email);
	}

	
	public User getUserById(int id) {
		 User user = userRepository.findById(id).orElse(null);
		    if (user != null) {
		        user.setPassword(null);  // remove password before sending to controller
		    }
		    return user;
	}
	
	public User getUserByOneName(String oneName) {
		User user = userRepository.findByOneName(oneName).orElse(null);
		if(user != null) {
			user.setPassword(null);
			user.setId(0);
			user.setEmail(null);
		}
		return user;
	}
	
	
	public List<User> getAllUsers() {
         List<User> users = userRepository.findAll();
    // Hide password
          users.forEach(user -> user.setPassword(null));
          return users;
    }

	// Delete profile picture
	public User deleteUserProfileImage(String email) {

	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    try {
	        if (user.getProfilePicPublicId() != null) {
	            cloudinary.uploader().destroy(
	                    user.getProfilePicPublicId(),
	                    ObjectUtils.asMap("resource_type", "image")
	            );

	            user.setProfilePicUrl(null);
	            user.setProfilePicPublicId(null);
	        }

	        return userRepository.save(user);

	    } catch (IOException e) {
	        throw new RuntimeException("Failed to delete profile picture", e);
	    }
	}
	
	
	public User verifypass(String email, String password) {
		return userRepository.verifyByUser(email, password);
	}

	// Delete user and all videos
	public void deleteUserByEmail(String email) {

	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    // 🔥 delete all user videos (secure version)
	    List<Video> videos = videoService.getVideoByUserIdNumber(user.getId());
	    for (Video v : videos) {
	        videoService.deleteVideoById(v.getVideoId(), email); // 🔥 pass email
	    }

	    // 🔥 delete profile pic
	    if (user.getProfilePicPublicId() != null) {
	        try {
	            cloudinary.uploader().destroy(
	                    user.getProfilePicPublicId(),
	                    ObjectUtils.asMap("resource_type", "image")
	            );
	        } catch (IOException e) {
	            System.err.println("Failed to delete profile pic: " + e.getMessage());
	        }
	    }

	    userRepository.delete(user);
	}
	
	

	// Other methods (updateDataUser, getAllUsers, getUserById, verifypass) remain
	// the same
	
	public User login(String email, String rawPassword) {

	    Optional<User> optionalUser = userRepository.findByEmail(email);

	    if (optionalUser.isEmpty()) {
	        return null;
	    }

	    User user = optionalUser.get();

	    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
	        return null;
	    }

	    return user;
	}
	
	public void sendVerificationOtp(String email) {

	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    if (user.isVerified()) {
	        throw new ApiException("Account already verified");
	    }

	    Optional<EmailVerificationOTP> existingOtpOpt = otpRepository.findByUser(user);

	    if (existingOtpOpt.isPresent()) {

	        EmailVerificationOTP existingOtp = existingOtpOpt.get();

	        // 🔥 BLOCK if requested within last 5 minutes
	        if (existingOtp.getCreatedAt()
	                .isAfter(LocalDateTime.now().minusMinutes(5))) {

	        	throw new ApiException("You can resend the OTP after 5 minutes");
	        }

	        otpRepository.delete(existingOtp);
	    }

	    String otp = String.valueOf(new Random().nextInt(900000) + 100000);

	    EmailVerificationOTP verificationOTP = new EmailVerificationOTP();
	    verificationOTP.setOtp(otp);
	    verificationOTP.setUser(user);
	    verificationOTP.setCreatedAt(LocalDateTime.now());
	    verificationOTP.setExpiryDate(LocalDateTime.now().plusMinutes(10));

	    otpRepository.save(verificationOTP);

	    emailService.sendOtpEmail(user.getEmail(), otp);
	}

	
	public void verifyOtp(String email, String otp) {

	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new ResponseStatusException(
	                    HttpStatus.NOT_FOUND,
	                    "User not found"
	            ));

	    EmailVerificationOTP verificationOTP = otpRepository.findByUser(user)
	            .orElseThrow(() -> new ResponseStatusException(
	                    HttpStatus.NOT_FOUND,
	                    "OTP not found"
	            ));

	    if (verificationOTP.getExpiryDate().isBefore(LocalDateTime.now())) {
	        throw new ResponseStatusException(
	                HttpStatus.BAD_REQUEST,
	                "OTP expired"
	        );
	    }

	    if (!verificationOTP.getOtp().equals(otp)) {
	        throw new ResponseStatusException(
	                HttpStatus.BAD_REQUEST,
	                "Invalid OTP"
	        );
	    }

	    user.setVerified(true);
	    userRepository.save(user);

	    otpRepository.delete(verificationOTP);
	}
	
	public User findByEmail(String email) {
	    return userRepository.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
	}



}
