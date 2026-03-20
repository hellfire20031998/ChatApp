package com.hellFire.Real_Time_Notifications_System.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "chats")
@Data
public class Chat extends BaseEntity {

    private Set<String> participantIds;

    @Indexed(unique = true)
    private String chatKey;

    private boolean isGroup;

    private String groupName;
}
