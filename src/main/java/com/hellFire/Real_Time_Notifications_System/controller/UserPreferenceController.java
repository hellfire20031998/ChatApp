package com.hellFire.Real_Time_Notifications_System.controller;

import com.hellFire.Real_Time_Notifications_System.dtos.UserPreferenceDto;
import com.hellFire.Real_Time_Notifications_System.dtos.request.UpdateUserPreferenceRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.response.ApiResponse;
import com.hellFire.Real_Time_Notifications_System.models.AppUsers;
import com.hellFire.Real_Time_Notifications_System.services.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserPreferenceDto>> getPreferences(
            @AuthenticationPrincipal AppUsers currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                userPreferenceService.getPreferences(currentUser.getId())
        ));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<UserPreferenceDto>> updatePreferences(
            @AuthenticationPrincipal AppUsers currentUser,
            @RequestBody UpdateUserPreferenceRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                userPreferenceService.updatePreferences(currentUser.getId(), request)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserPreferenceDto>> createOrReplacePreferences(
            @AuthenticationPrincipal AppUsers currentUser,
            @RequestBody UpdateUserPreferenceRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                userPreferenceService.createOrReplacePreferences(currentUser.getId(), request)
        ));
    }
}
