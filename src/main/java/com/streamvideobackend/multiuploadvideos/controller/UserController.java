package com.streamvideobackend.multiuploadvideos.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.streamvideobackend.multiuploadvideos.dto.ApiResponse1;
import com.streamvideobackend.multiuploadvideos.dto.LoginRequest;
import com.streamvideobackend.multiuploadvideos.dto.User;
import com.streamvideobackend.multiuploadvideos.service.PasswordResetService;
import com.streamvideobackend.multiuploadvideos.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserService userService;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    
    @GetMapping("/health")
    public String health() {
    	System.out.println("Ready set go!");
    	return "ok";
    }

    // Create user with optional profile picture
    @PostMapping("/user")
    public ResponseEntity<ApiResponse1> postUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String biodetails,
            @RequestParam String country,
            @RequestParam(required = false) MultipartFile profilePic) {

        try {
            User user = userService.postUser(name, email, password, biodetails, country, profilePic);

            return ResponseEntity.ok(
                    new ApiResponse1(true, "User created successfully", user)
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse1(false, e.getMessage(), null)
            );

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new ApiResponse1(false, "Something went wrong", null)
            );
        }
    }

    // Update profile picture
    @PatchMapping("/user/{id}/profile-pic")
    public ResponseEntity<User> updateProfileImage(@PathVariable int id,
                                                   @RequestParam MultipartFile profilePic) {
        try {
            User user = userService.updateUserProfileImage(id, profilePic);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Update user data (name, biodetails, country)
    
    @PatchMapping("/user/{id}/details")
    public ResponseEntity<User> updateUserDetails(@PathVariable int id,
                                                  @RequestParam String name,
                                                  
                                                  @RequestParam String biodetails,
                                                  @RequestParam String country) {
        try {
            User user = userService.updateDataUser(id, name, biodetails, country);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
   }
    
    
    //changing the password 
    @PatchMapping("/user/{id}/updatePassword")
    public ResponseEntity<?> userPasswordChange(
            @PathVariable int id,
            @RequestParam String newPassword,
            @RequestParam String oldPassword) {

        try {
            User user = userService.updateUserPassword(newPassword, id, oldPassword);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

     //Get all users
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Get user by ID
    @GetMapping("/user/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    // Verify user by email & password
    @PostMapping("/verify-by-pass")
    public ResponseEntity<User> verifyUser(@RequestParam String email, @RequestParam String password) {
        User user = userService.verifypass(email, password);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    // Delete user and all associated videos & profile pic
    @DeleteMapping("/user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {
        try {
            userService.deleteUserById(id);
            return ResponseEntity.ok("Deleted Successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Delete only profile picture
    @DeleteMapping("/user/{id}/profile-pic")
    public ResponseEntity<String> deleteProfilePicture(@PathVariable int id) {
        try {
            userService.deleteUserProfileImage(id);
            return ResponseEntity.ok("Profile picture deleted successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Error deleting profile picture: " + e.getMessage());
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        passwordResetService.processForgotPassword(email);
        return ResponseEntity.ok("If an account exists, a reset link has been sent ✅");
    }

    // Reset password endpoint
//    @PostMapping("/reset-password")
//    public ResponseEntity<String> resetPassword(@RequestParam String token,
//                                                @RequestParam String newPassword) {
//        passwordResetService.resetPassword(token, newPassword);
//        return ResponseEntity.ok("Password has been successfully reset ✅");
//    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        try {
            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok("Password reset successful.");
        } catch (RuntimeException ex) {
            String msg = ex.getMessage();

            // Customize messages for known cases
            if ("Invalid token".equals(msg)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token.");
            }
            if ("Token expired".equals(msg)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token expired.");
            }

            // Fallback for other unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error.");
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        User user = userService.login(request.getEmail(), request.getPassword());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }

        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {

        userService.sendVerificationOtp(email);

        return ResponseEntity.ok("OTP sent successfully");
    }
    
    
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {

        userService.verifyOtp(email, otp);

        return ResponseEntity.ok("Account verified successfully");
    }




    
    

}
