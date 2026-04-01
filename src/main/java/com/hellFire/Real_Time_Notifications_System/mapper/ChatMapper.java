package com.hellFire.Real_Time_Notifications_System.mapper;

import com.hellFire.Real_Time_Notifications_System.dtos.ChatDto;
import com.hellFire.Real_Time_Notifications_System.dtos.UserDto;
import com.hellFire.Real_Time_Notifications_System.models.Chat;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ChatMapper {

    public ChatDto toDto(
            Chat chat,
            String currentUserId,
            Map<String, UserDto> userMap,
            String lastMessage,
            LocalDateTime lastMessageTime,
            int unreadCount
    ) {

        if (chat == null) return null;

        ChatDto dto = new ChatDto();

        dto.setId(chat.getId());
        dto.setCreatedAt(chat.getCreatedAt());
        dto.setUpdatedAt(chat.getUpdatedAt());

        dto.setGroup(chat.isGroup());
        dto.setGroupName(chat.getGroupName());

        if (!chat.isGroup()) {
            String otherUserId = chat.getParticipantIds()
                    .stream()
                    .filter(id -> !id.equals(currentUserId))
                    .findFirst()
                    .orElse(null);

            if (otherUserId != null) {
                dto.setOtherUser(userMap.get(otherUserId));
            }
        }


        if (chat.isGroup()) {
            List<UserDto> participants = chat.getParticipantIds()
                    .stream()
                    .map(userMap::get)
                    .toList();

            dto.setParticipants(participants);
            dto.setCreatedBy(chat.getCreatedBy());
            Set<String> adminIds = chat.getAdminIds() == null ? Set.of() : new HashSet<>(chat.getAdminIds());
            dto.setAdminIds(adminIds);
            dto.setCanManageGroup(adminIds.contains(currentUserId));
        }

        dto.setLastMessage(lastMessage);
        dto.setLastMessageTime(lastMessageTime);
        dto.setUnreadCount(unreadCount);

        return dto;
    }
}