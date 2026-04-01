package com.hellFire.Real_Time_Notifications_System.dtos.request;

import lombok.Data;

@Data
public class TypingEventRequest {
    private String chatId;
    private String receiverId;
    private boolean typing;
}
