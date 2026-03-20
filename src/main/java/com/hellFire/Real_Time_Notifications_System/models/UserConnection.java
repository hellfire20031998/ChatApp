package com.hellFire.Real_Time_Notifications_System.models;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "user_connections")
public class UserConnection extends BaseEntity {

    private String userId;

    private List<Connection> connections;

    public static class Connection extends BaseEntity{
        private String connectedUserId;
        private String chatId;
        private boolean isBlocked;
        private boolean isPinned;
    }
}
