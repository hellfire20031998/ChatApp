package com.hellFire.Real_Time_Notifications_System.models;

import com.hellFire.Real_Time_Notifications_System.models.enums.MessageStatus;
import com.hellFire.Real_Time_Notifications_System.models.enums.MessageType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "messages")
@Data
public class Message extends BaseEntity {

    private String chatId;
    private String senderId;
    private String receiverId;
    private String content;
    private LocalDateTime timeStamp;
    private MessageStatus status;
    private MessageType type;
}