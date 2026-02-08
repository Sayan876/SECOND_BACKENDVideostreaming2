package com.streamvideobackend.multiuploadvideos.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.streamvideobackend.multiuploadvideos.dto.User;
import com.streamvideobackend.multiuploadvideos.dto.Video;
import com.streamvideobackend.multiuploadvideos.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final VideoService videoService;

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

	// Create user with profile pic uploaded to Cloudinary
	public User postUser(String name, String email, String password, String biodetails, String country,
			MultipartFile profilePic) {

		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPassword(password);
		user.setBiodetails(biodetails);
		user.setCountry(country);

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
	public User updateUserProfileImage(int userId, MultipartFile newProfilePic) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

		try {
			// Delete old image from Cloudinary
			if (user.getProfilePicPublicId() != null) {
				cloudinary.uploader().destroy(user.getProfilePicPublicId(),
						ObjectUtils.asMap("resource_type", "image"));
			}

			// Upload new image
			Map uploadResult = cloudinary.uploader().upload(newProfilePic.getBytes(),
					ObjectUtils.asMap("resource_type", "image", "folder", "profilePics"));

			user.setProfilePicUrl(uploadResult.get("secure_url").toString());
			user.setProfilePicPublicId(uploadResult.get("public_id").toString());

			return userRepository.save(user);

		} catch (IOException e) {
			throw new RuntimeException("Failed to update profile image for user ID: " + userId, e);
		}
	}

	//Update user details 
	public User updateDataUser(int id, String name, String biodetials, String country) {
		Optional<User> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new RuntimeException("User not found with ID: " + id);
		}

		User user = optionalUser.get();
		try {
			user.setName(name);
			
			user.setBiodetails(biodetials);
			user.setCountry(country);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return userRepository.save(user);

	}


	//Change password 
    public User updateUserPassword(String newPassword, int id, String oldPassword) {

	    User user = userRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    // VALIDATE OLD PASSWORD
	    if (!user.getPassword().equals(oldPassword)) {
	        throw new RuntimeException("Old password is incorrect");
	    }

	    user.setPassword(newPassword);
	    return userRepository.save(user);
	}
	
	public User getUserById(int id) {
		 User user = userRepository.findById(id).orElse(null);
		    if (user != null) {
		        user.setPassword(null);  // remove password before sending to controller
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
	public User deleteUserProfileImage(int userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

		try {
			if (user.getProfilePicPublicId() != null) {
				cloudinary.uploader().destroy(user.getProfilePicPublicId(),
						ObjectUtils.asMap("resource_type", "image"));
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
	public void deleteUserById(int id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));

		// Delete videos
		List<Video> videos = videoService.getVideoByUserIdNumber(id);
		for (Video v : videos) {
			videoService.deleteVideoById(v.getVideoId());
		}

		// Delete profile pic from Cloudinary
		if (user.getProfilePicPublicId() != null) {
			try {
				cloudinary.uploader().destroy(user.getProfilePicPublicId(),
						ObjectUtils.asMap("resource_type", "image"));
			} catch (IOException e) {
				System.err.println("Failed to delete user profile pic: " + e.getMessage());
			}
		}

		userRepository.delete(user);
	}

	// Other methods (updateDataUser, getAllUsers, getUserById, verifypass) remain
	// the same
}
