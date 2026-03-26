package com.hellFire.Real_Time_Notifications_System.mapper;

import com.hellFire.Real_Time_Notifications_System.dtos.MessageDto;
import com.hellFire.Real_Time_Notifications_System.dtos.request.UpdateMessageRequest;
import com.hellFire.Real_Time_Notifications_System.models.Message;
import com.hellFire.Real_Time_Notifications_System.services.cache.UserCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MessageMapper {

    private final UserCacheService userCacheService;

    public MessageDto toDto(Message message) {
        if (message == null) {
            return null;
        }

        MessageDto dto = new MessageDto();

        dto.setId(message.getId());
        dto.setChatId(message.getChatId());
        dto.setSenderId(message.getSenderId());
        dto.setReceiverId(message.getReceiverId());
        if (message.getSenderId() != null) {
            dto.setSender(userCacheService.getUsersByIds(Set.of(message.getSenderId()))
                    .get(message.getSenderId()));
        }
        dto.setContent(message.getContent());
        dto.setTimeStamp(message.getTimeStamp());
        dto.setStatus(message.getStatus());
        dto.setType(message.getType());

        dto.setCreatedAt(message.getCreatedAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        dto.setDeleted(message.isDeleted());

        return dto;
    }

    public List<MessageDto> toDtoList(List<Message> messageList) {

        if (messageList == null || messageList.isEmpty()) {
            return List.of();
        }

        return messageList.stream()
                .map(this::toDto)
                .toList();
    }

    public void updateEntity(Message message, UpdateMessageRequest request){
        if (message == null || request == null) {
            return;
        }

        if (request.getContent() != null) {
            message.setContent(request.getContent());
        }

        if (request.getType() != null) {
            message.setType(request.getType());
        }
    }
}
