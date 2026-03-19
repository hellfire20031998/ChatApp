package com.hellFire.Real_Time_Notifications_System.models;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "messages")
public class Message extends BaseEntity {

    private String chatId;
    private String senderId;
    private String content;
    private Instant timestamp;
    private boolean read;
}