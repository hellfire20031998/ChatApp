package com.hellFire.Real_Time_Notifications_System.dtos.request;

import com.hellFire.Real_Time_Notifications_System.models.enums.MessageType;
import lombok.Data;

@Data
public class UpdateMessageRequest {
    private String content;
    private MessageType type;
}
