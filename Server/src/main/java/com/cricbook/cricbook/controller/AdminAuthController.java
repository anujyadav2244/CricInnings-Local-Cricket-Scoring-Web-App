package com.cricbook.cricbook.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.cricbook.cricbook.model.Admin;
import com.cricbook.cricbook.service.AdminService;

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

    // ----------------- VERIFY OTP -----------------
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody Map<String, String> request) {
        String message = adminService.verifyOtp(request.get("email"), request.get("otp"));
        return ResponseEntity.ok(Map.of("message", message));
    }

    // ----------------- LOGIN -----------------
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        Map<String, String> loginResponse = adminService.login(request.get("email"), request.get("password"));
        return ResponseEntity.ok(loginResponse);
    }

    // ----------------- CURRENT USER -----------------
    @GetMapping("/me")
    public ResponseEntity<Admin> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Admin admin = adminService.getCurrentUser(email);
        return ResponseEntity.ok(admin);
    }

    // ----------------- UPDATE PROFILE -----------------
    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateProfile(@RequestBody Map<String, String> updates) {
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
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
        adminService.logout(token); // blacklist token
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // ----------------- DELETE ACCOUNT -----------------
    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        String message = adminService.deleteCurrentUser(email);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
