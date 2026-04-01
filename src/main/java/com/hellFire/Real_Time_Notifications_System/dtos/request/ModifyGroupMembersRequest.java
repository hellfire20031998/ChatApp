package com.hellFire.Real_Time_Notifications_System.dtos.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ModifyGroupMembersRequest {
    @NotEmpty(message = "participantIds must not be empty")
    private List<String> participantIds;
}
