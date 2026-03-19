package com.hellFire.Real_Time_Notifications_System.models;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "chats")
public class Chat extends BaseEntity {

    private List<String> participantIds;

    private boolean isGroup;

    private String groupName;
}
