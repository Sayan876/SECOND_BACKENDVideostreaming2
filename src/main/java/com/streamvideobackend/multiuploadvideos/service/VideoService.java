package com.streamvideobackend.multiuploadvideos.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public Video saveVideo(Video video, MultipartFile file, int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "folder", "videos/user_" + userId
                    ));

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
    public Video updatetanddec(String title, String description, String videoId) {
        Optional<Video> recVideo = videoRepository.findById(videoId);
        if (recVideo.isEmpty()) return null;

        Video video = recVideo.get();
        video.setTitle(title);
        video.setDescription(description);
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

    public List<Video> getVideoByUserIdNumber(int id) {
        return videoRepository.getVideosByUserId(id);
    }
    
    
    

    //for searching title
    public List<Video> getVideoByTitle(String title) {
        return videoRepository.getVideosByTitle(title);
    }

    public User getUserByVideoId(String videoId) {
        return videoRepository.findUserByVideoId(videoId);
    }

    // Delete video from Cloudinary and DB
    public boolean deleteVideoById(String videoId) {
        Optional<Video> recVideo = videoRepository.findById(videoId);
        if (recVideo.isEmpty()) return false;

        Video video = recVideo.get();
        try {
            cloudinary.uploader().destroy(video.getPublicId(), ObjectUtils.asMap("resource_type", "video"));
            videoRepository.delete(video);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting video: " + e.getMessage());
            return false;
        }
    }

    public int getUserIdByVideoId(String videoId) {
        return videoRepository.findUserIdByVideoId(videoId);
    }

    public List<Video> getVideosbyEmailandPass(String email, String pass) {
        return videoRepository.getVideosByUserEmailandPassword(email, pass);
    }
}
