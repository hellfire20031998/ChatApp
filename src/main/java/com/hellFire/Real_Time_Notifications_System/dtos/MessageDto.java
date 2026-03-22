package com.hellFire.Real_Time_Notifications_System.dtos;

import com.hellFire.Real_Time_Notifications_System.models.enums.MessageType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class MessageDto extends BaseEntityDto{
    private String chatId;
    private String senderId;
    private String receiverId;
    private String content;
    private LocalDateTime timeStamp;
    private MessageType type;
}
