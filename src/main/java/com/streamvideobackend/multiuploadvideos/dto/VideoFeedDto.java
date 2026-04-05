package com.streamvideobackend.multiuploadvideos.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoFeedDto {

    private String videoId;
    private String title;
    private String description;
    private String videoUrl;
    private LocalDateTime uploadedAt;

    private int id;               // user id
    private String name;          // username
    private String profilePicUrl;
    
}
