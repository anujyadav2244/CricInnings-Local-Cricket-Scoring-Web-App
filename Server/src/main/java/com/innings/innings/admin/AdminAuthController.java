package com.innings.innings.admin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${app.allowed.origins:http://localhost:5173}", allowCredentials = "true")
public class AdminAuthController {

    @Autowired
    private AdminService adminService;

    // ----------------- SIGNUP -----------------
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody Admin admin) {
        String message = adminService.signup(admin);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // ----------------- VERIFY SIGNUP OTP -----------------
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody Map<String, String> request) {
        String message = adminService.verifyOtp(request.get("email"), request.get("otp"));
        return ResponseEntity.ok(Map.of("message", message));
    }

    // ----------------- LOGIN -----------------
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        try {
            Map<String, String> loginResponse = adminService.login(request.get("email"), request.get("password"));
            return ResponseEntity.ok(loginResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------- FORGOT PASSWORD -----------------
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String message = adminService.forgotPassword(email);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // ----------------- VERIFY FORGOT PASSWORD OTP -----------------
    @PostMapping("/verify-forgot-otp")
    public ResponseEntity<Map<String, String>> verifyForgotOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");
        String message = adminService.verifyForgotOtp(email, otp, newPassword);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // ----------------- RESET PASSWORD (AUTH REQUIRED) -----------------
    @PutMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "Authorization token required"));
        }

        String token = authHeader.substring(7);
        String email = adminService.getEmailFromToken(token);
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        String message = adminService.resetPassword(email, oldPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // ----------------- GET CURRENT USER -----------------
    @GetMapping("/me")
    public ResponseEntity<Admin> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        String email = adminService.getEmailFromToken(token);
        Admin admin = adminService.getCurrentUser(email);
        return ResponseEntity.ok(admin);
    }

    // ----------------- UPDATE PROFILE -----------------
    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> updates) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "Authorization token required"));
        }
        String token = authHeader.substring(7);
        String email = adminService.getEmailFromToken(token);

        String message = adminService.updateProfile(email, updates);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // ----------------- LOGOUT -----------------
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token required"));
        }

        String token = authHeader.substring(7);
        adminService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // ----------------- DELETE ACCOUNT -----------------
    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteCurrentUser(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "Authorization token required"));
        }
        String token = authHeader.substring(7);
        String email = adminService.getEmailFromToken(token);

        String message = adminService.deleteCurrentUser(email);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
