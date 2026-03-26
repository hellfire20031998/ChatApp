package com.hellFire.Real_Time_Notifications_System.dtos;

import com.hellFire.Real_Time_Notifications_System.models.enums.MessageStatus;
import com.hellFire.Real_Time_Notifications_System.models.enums.MessageType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class MessageDto extends BaseEntityDto{
    private String chatId;
    private String senderId;
    private String receiverId;
    private UserDto sender;
    private String content;
    private LocalDateTime timeStamp;
    private MessageStatus status;
    private MessageType type;
}
