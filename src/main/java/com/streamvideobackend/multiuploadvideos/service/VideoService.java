package com.streamvideobackend.multiuploadvideos.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.streamvideobackend.multiuploadvideos.dto.User;
import com.streamvideobackend.multiuploadvideos.dto.Video;
import com.streamvideobackend.multiuploadvideos.repository.UserRepository;
import com.streamvideobackend.multiuploadvideos.repository.VideoRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    private Cloudinary cloudinary;

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @PostConstruct
    public void initCloudinary() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    // Upload video to Cloudinary and save in DB
    public Video saveVideo(Video video, MultipartFile file, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ verification check stays same
        if (!user.isVerified()) {
            throw new RuntimeException("Please verify your account before uploading videos");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getInputStream(),
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "folder", "videos/user_" + user.getId()
                    )
            );

            video.setVideoUrl(uploadResult.get("secure_url").toString());
            video.setPublicId(uploadResult.get("public_id").toString());
            video.setContentType(file.getContentType());
            video.setUser(user);

            return videoRepository.save(video);

        } catch (IOException e) {
            throw new RuntimeException("Video upload failed", e);
        }
    }

    // Update title and description
    public Video updatetanddec(String title, String description, String category, String videoId, String email) {

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        // 🔥 OWNER CHECK
        if (!video.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not allowed to edit this video");
        }

        video.setTitle(title);
        video.setDescription(description);
        video.setCategory(category);

        return videoRepository.save(video);
    }

    // Get all videos, newest first
    public List<Video> getAllLatestToNew() {
        return videoRepository.findAllVideosOrderByUploadedAtDesc();
    }

    public Video getVideoById(String id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));
    }
    
    public List<Video> getVideosByCategory(String category) {
        return videoRepository.findByCategoryIgnoreCase(category);
    }

    public List<Video> getVideoByUserIdNumber(int id) {
        return videoRepository.getVideosByUserId(id);
    }
    
    public List<Video> getVideosByOneName(String oneName){
    	return videoRepository.getVideosByOneName(oneName);
    }
    

    //for searching title
    public List<Video> getVideoByTitle(String title) {
    return videoRepository.findByTitleContainingIgnoreCase(title);
}

    public User getUserByVideoId(String videoId) {
        User user =  videoRepository.findUserByVideoId(videoId);
        if(user!=null) {
        	user.setId(0);
        	user.setPassword(null);
        	user.setEmail(null);
        	return user;
        }
        return null;
    }

    // Delete video from Cloudinary and DB
    public boolean deleteVideoById(String videoId, String email) {

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        // 🔥 OWNER CHECK
        if (!video.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not allowed to delete this video");
        }

        try {
            cloudinary.uploader().destroy(
                    video.getPublicId(),
                    ObjectUtils.asMap("resource_type", "video")
            );

            videoRepository.delete(video);
            return true;

        } catch (Exception e) {
            throw new RuntimeException("Error deleting video: " + e.getMessage());
        }
    }

    public int getUserIdByVideoId(String videoId) {
        return videoRepository.findUserIdByVideoId(videoId);
    }

    public List<Video> getVideosbyEmailandPass(String email, String pass) {
        return videoRepository.getVideosByUserEmailandPassword(email, pass);
    }
    
    public List<Video> getAllVideosWithUploader() {
        return videoRepository.findAllVideosWithUsers1();
    }
    
    public User findByEmail(String email) {
	    return userRepository.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
	}
    
    
}
