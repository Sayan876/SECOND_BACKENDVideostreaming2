package com.streamvideobackend.multiuploadvideos.controller;

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
@CrossOrigin("https://video-streaming-frontend-eight.vercel.app")
@RequiredArgsConstructor
public class VideoControl {

    private final VideoService videoService;

    // Create/upload video
    @PostMapping("/{id}")
    public ResponseEntity<?> create(@RequestParam("file") MultipartFile file,
                                    @RequestParam("title") String title,
                                    @RequestParam("description") String description,
                                    @PathVariable int id) {

        Video video = Video.builder()
                .videoId(UUID.randomUUID().toString())
                .title(title)
                .description(description)
                .build();

        Video savedVideo = videoService.saveVideo(video, file, id);
        return ResponseEntity.ok(savedVideo);
    }

    // Update title & description
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateVideo(@PathVariable String id,
                                         @RequestParam("title") String title,
                                         @RequestParam("description") String description) {

        Video updated = videoService.updatetanddec(title, description, id);
        if (updated == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video not found");

        return ResponseEntity.ok(updated);
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

    // Search videos by title
    @GetMapping("/searchByTitle/{title}")
    public ResponseEntity<List<Video>> searchByTitle(@PathVariable String title) {
        return ResponseEntity.ok(videoService.getVideoByTitle(title));
    }

    // Delete video
    @DeleteMapping("/{videoId}")
    public ResponseEntity<String> deleteVideo(@PathVariable String videoId) {
        boolean deleted = videoService.deleteVideoById(videoId);
        if (deleted)
            return ResponseEntity.ok("Video deleted successfully");
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video not found or could not be deleted");
    }

    // Get user ID by video ID
    @GetMapping("/getUserIdByVideoId/{videoId}")
    public ResponseEntity<Integer> getUserId(@PathVariable String videoId) {
        return ResponseEntity.ok(videoService.getUserIdByVideoId(videoId));
    }

    // Get user by video ID
    @GetMapping("/getUserByVideo/{videoId}")
    public ResponseEntity<User> getUserByVideo(@PathVariable String videoId) {
        User user = videoService.getUserByVideoId(videoId);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(user);
    }

    // Verify videos by email & password (dev/testing only)
    @PostMapping("/verifyep")
    public ResponseEntity<List<Video>> verifyByEmailAndPass(@RequestParam String email,
                                                            @RequestParam String password) {
        List<Video> result = videoService.getVideosbyEmailandPass(email, password);
        if (result == null || result.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }
}
