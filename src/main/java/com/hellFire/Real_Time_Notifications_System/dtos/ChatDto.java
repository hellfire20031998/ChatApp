package com.hellFire.Real_Time_Notifications_System.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data

public class ChatDto extends BaseEntityDto {

    private boolean isGroup;
    private String groupName;

    private UserDto otherUser;

    private List<UserDto> participants;

    private String lastMessage;
    private LocalDateTime lastMessageTime;

    private int unreadCount;
}
