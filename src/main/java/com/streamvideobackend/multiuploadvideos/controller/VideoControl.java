package com.streamvideobackend.multiuploadvideos.controller;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.streamvideobackend.multiuploadvideos.dto.User;
import com.streamvideobackend.multiuploadvideos.dto.Video;
import com.streamvideobackend.multiuploadvideos.playload.CustomMessage;
import com.streamvideobackend.multiuploadvideos.service.VideoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v4/videos")
@CrossOrigin("*")
@RequiredArgsConstructor
public class VideoControl {

    private final VideoService videoService;

    // Create/upload video
    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            Authentication auth
    ) {

        String email = auth.getName();

        Video video = Video.builder()
                .videoId(UUID.randomUUID().toString())
                .title(title)
                .description(description)
                .category(category)
                .build();

        Video savedVideo = videoService.saveVideo(video, file, email);

        return ResponseEntity.ok(savedVideo);
    }

    // Update title & description
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateVideo(
            @PathVariable String id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            Authentication auth
    ) {

        try {
            String email = auth.getName();

            Video updated = videoService.updatetanddec(
                    title,
                    description,
                    category,
                    id,
                    email
            );

            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // Get all videos newest first
    @GetMapping
    public ResponseEntity<List<Video>> showAllVideos() {
        List<Video> videos = videoService.getAllLatestToNew();
        return ResponseEntity.ok(videos);
    }

    // Get videos by user ID
    @GetMapping("/byUserId/{id}")
    public ResponseEntity<List<Video>> getVideosByUser(@PathVariable int id) {
        return ResponseEntity.ok(videoService.getVideoByUserIdNumber(id));
    }
    
    @GetMapping("/byOneName/{oneName}")
    public ResponseEntity<List<Video>> getVideosByOneName(@PathVariable String oneName){
    	return ResponseEntity.ok(videoService.getVideosByOneName(oneName));
    }

    // Search videos by title
    @GetMapping("/searchByTitle/{title}")
    public ResponseEntity<List<Video>> searchByTitle(@PathVariable String title) {
        return ResponseEntity.ok(videoService.getVideoByTitle(title));
    }

    // Delete video
    @DeleteMapping("/{videoId}")
    public ResponseEntity<String> deleteVideo(
            @PathVariable String videoId,
            Authentication auth
    ) {
        try {
            String email = auth.getName();

            videoService.deleteVideoById(videoId, email);

            return ResponseEntity.ok("Video deleted successfully");

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }

    // Get user ID by video ID
//    @GetMapping("/getUserIdByVideoId/{videoId}")
//    public ResponseEntity<Integer> getUserId(@PathVariable String videoId) {
//        return ResponseEntity.ok(videoService.getUserIdByVideoId(videoId));
//    }

    // Get user by video ID
    @GetMapping("/getUserByVideo/{videoId}")
    public ResponseEntity<User> getUserByVideo(@PathVariable String videoId) {
        User user = videoService.getUserByVideoId(videoId);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(user);
    }

    // Verify videos by email & password (dev/testing only)
//    @PostMapping("/verifyep")
//    public ResponseEntity<List<Video>> verifyByEmailAndPass(@RequestParam String email,
//                                                            @RequestParam String password) {
//        List<Video> result = videoService.getVideosbyEmailandPass(email, password);
//        if (result == null || result.isEmpty())
//            return ResponseEntity.notFound().build();
//        return ResponseEntity.ok(result);
//    }
    
    @GetMapping("/feed")
    public List<Video> getVideoFeed() {
        return videoService.getAllVideosWithUploader();
    }
    
    @GetMapping("/byVideoId/{videoId}")
    public ResponseEntity<Video> getVideoById(@PathVariable String videoId){
    	Video video = videoService.getVideoById(videoId);
    	if(video == null) {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    	}
    	return ResponseEntity.ok(video);
    } 
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Video>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(videoService.getVideosByCategory(category));
    }
}
