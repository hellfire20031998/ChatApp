package com.hellFire.Real_Time_Notifications_System.controller;

import com.hellFire.Real_Time_Notifications_System.dtos.request.CreateUserRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.request.LoginUserRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.response.ApiResponse;
import com.hellFire.Real_Time_Notifications_System.dtos.response.UserResponse;
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
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody LoginUserRequest request) {

        UserResponse response = service.login(request.getEmail(), request.getPassword());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}