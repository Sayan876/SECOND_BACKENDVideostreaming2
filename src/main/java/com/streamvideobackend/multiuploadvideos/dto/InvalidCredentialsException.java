package com.streamvideobackend.multiuploadvideos.dto;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
