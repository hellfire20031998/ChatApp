package com.hellFire.Real_Time_Notifications_System.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartChatRequest {

    @NotBlank(message = "UserId is required")
    private String userId;
}