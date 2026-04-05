package com.streamvideobackend.multiuploadvideos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.streamvideobackend.multiuploadvideos.service.EmailService;

@RestController
public class EmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/send-test")
    public String sendMail() {
        emailService.sendTestEmail();
        return "Email Sent!";
    }
}

