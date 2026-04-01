package com.hellFire.Real_Time_Notifications_System.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateGroupRequest {
    @NotBlank(message = "Group name is required")
    private String groupName;

    @NotEmpty(message = "At least one member is required")
    private List<String> participantIds;
}
