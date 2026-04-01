package com.hellFire.Real_Time_Notifications_System.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RenameGroupRequest {
    @NotBlank(message = "Group name is required")
    private String groupName;
}
