package com.hellFire.Real_Time_Notifications_System.services;

import com.hellFire.Real_Time_Notifications_System.dtos.MessageDto;
import com.hellFire.Real_Time_Notifications_System.dtos.request.ChatMessageRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.request.UpdateMessageRequest;
import com.hellFire.Real_Time_Notifications_System.mapper.MessageMapper;
import com.hellFire.Real_Time_Notifications_System.models.Message;
import com.hellFire.Real_Time_Notifications_System.models.enums.MessageStatus;
import com.hellFire.Real_Time_Notifications_System.repositories.IMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final IMessageRepository messageRepository;
    private final MessageMapper messageMapper;

    public MessageDto saveAndSendMessage(ChatMessageRequest message, String sender) {

        Message entity = Message.builder()
                .chatId(message.getChatId())
                .senderId(sender)
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .timeStamp(LocalDateTime.now())
                .type(message.getType())
                .status(MessageStatus.SENT)
                .build();

        Message saved = messageRepository.save(entity);

        return messageMapper.toDto(saved);
    }

    public MessageDto updateMessage(String messageId, UpdateMessageRequest request, String requesterId) {
        Message message = getAuthorizedMessage(messageId, requesterId);
        messageMapper.updateEntity(message, request);
        Message saved = messageRepository.save(message);
        return messageMapper.toDto(saved);
    }

    public MessageDto deleteMessage(String messageId, String requesterId) {
        Message message = getAuthorizedMessage(messageId, requesterId);
        message.setDeleted(true);
        Message saved = messageRepository.save(message);
        return messageMapper.toDto(saved);
    }

    private Message getAuthorizedMessage(String messageId, String requesterId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (message.isDeleted()) {
            throw new RuntimeException("Message already deleted");
        }

        if (!message.getSenderId().equals(requesterId)) {
            throw new RuntimeException("You are not allowed to modify this message");
        }

        return message;
    }
}
