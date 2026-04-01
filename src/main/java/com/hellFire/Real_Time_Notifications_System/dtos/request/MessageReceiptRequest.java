package com.hellFire.Real_Time_Notifications_System.dtos.request;

import lombok.Data;

@Data
public class MessageReceiptRequest {
    private String messageId;
    private String chatId;
}
