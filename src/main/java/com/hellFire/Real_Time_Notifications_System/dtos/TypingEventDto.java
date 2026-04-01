package com.hellFire.Real_Time_Notifications_System.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TypingEventDto {
    private String eventType;
    private String chatId;
    private String senderId;
    private String receiverId;
    private boolean typing;
}
