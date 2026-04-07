package com.hellFire.Real_Time_Notifications_System.controller;

import com.hellFire.Real_Time_Notifications_System.dtos.request.CreateUserRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.request.LoginUserRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.request.ResendVerificationRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.request.VerifyEmailRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.response.ApiResponse;
import com.hellFire.Real_Time_Notifications_System.dtos.response.UserResponse;
import jakarta.validation.Valid;
import com.hellFire.Real_Time_Notifications_System.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginUserRequest request) {

        UserResponse response = service.login(request.getEmail(), request.getPassword());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.verifyEmail(request.getToken())));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.resendVerificationEmail(request.getEmail())));
    }
}