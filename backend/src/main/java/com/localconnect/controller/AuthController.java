package com.localconnect.controller;

import com.localconnect.domain.User;
import com.localconnect.dto.AuthRequest;
import com.localconnect.dto.AuthResponse;
import com.localconnect.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/otp/send")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        authService.sendOtp(phone);
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully to " + phone));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginOrRegister(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.loginOrRegister(request));
    }

    @PostMapping("/switch-role")
    public ResponseEntity<AuthResponse> switchRole(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {
        String targetRole = body.get("role");
        return ResponseEntity.ok(authService.switchRole(user.getId(), targetRole));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.getCurrentUserResponse(user.getId()));
    }
}
