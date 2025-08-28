package com.cricbook.cricbook.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cricbook.cricbook.model.Admin;
import com.cricbook.cricbook.repository.AdminRepository;
import com.cricbook.cricbook.security.JwtBlacklistService;
import com.cricbook.cricbook.security.JwtUtil;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtBlacklistService jwtBlacklistService;

    // ----------------- SIGNUP -----------------
    public String signup(Admin admin) {
        if (adminRepository.findByEmail(admin.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered!");
        }

        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        admin.setOtp(otp);
        admin.setOtpGeneratedAt(LocalDateTime.now());
        admin.setVerified(false);
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));

        // send OTP email
        emailService.sendOtpEmail(admin.getEmail(), otp);

        adminRepository.save(admin);
        return "OTP sent to email!";
    }

    // ----------------- VERIFY OTP -----------------
    public String verifyOtp(String email, String otp) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (admin.getVerified()) return "User already verified!";
        if (!otp.equals(admin.getOtp())) throw new RuntimeException("Invalid OTP!");
        if (admin.getOtpGeneratedAt() == null || admin.getOtpGeneratedAt().plusMinutes(10).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired!");
        }

        admin.setVerified(true);
        admin.setOtp(null);
        admin.setOtpGeneratedAt(null);
        adminRepository.save(admin);

        return "Email verified successfully!";
    }

    // ----------------- LOGIN -----------------
    public Map<String, String> login(String email, String password) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!admin.getVerified()) throw new RuntimeException("Email not verified");
        if (!passwordEncoder.matches(password, admin.getPassword()))
            throw new RuntimeException("Invalid credentials");

        String token = jwtUtil.generateToken(email);
        return Map.of(
                "token", token,
                "userId", admin.getId()
        );
    }

    // ----------------- GET CURRENT USER -----------------
    public Admin getCurrentUser(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ----------------- UPDATE PROFILE -----------------
    public String updateProfile(String email, Map<String, String> updates) {
        Admin admin = getCurrentUser(email);
        if (updates.containsKey("name")) admin.setName(updates.get("name"));
        if (updates.containsKey("password")) admin.setPassword(passwordEncoder.encode(updates.get("password")));
        adminRepository.save(admin);
        return "Profile updated successfully";
    }

    // ----------------- DELETE ACCOUNT -----------------
    public String deleteCurrentUser(String email) {
        Admin admin = getCurrentUser(email);
        adminRepository.delete(admin);
        return "User account deleted successfully";
    }
    // ----------------- LOGOUT -----------------
    public void logout(String token) {
        if (token != null && !jwtBlacklistService.isBlacklisted(token)) {
            jwtBlacklistService.blacklistToken(token);
        }
    }

}
