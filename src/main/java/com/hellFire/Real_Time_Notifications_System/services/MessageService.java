package com.hellFire.Real_Time_Notifications_System.services;

import com.hellFire.Real_Time_Notifications_System.dtos.MessageDto;
import com.hellFire.Real_Time_Notifications_System.dtos.request.ChatMessageRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.request.UpdateMessageRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.response.AllMessagesForChat;
import com.hellFire.Real_Time_Notifications_System.mapper.MessageMapper;
import com.hellFire.Real_Time_Notifications_System.models.Message;
import com.hellFire.Real_Time_Notifications_System.models.enums.MessageStatus;
import com.hellFire.Real_Time_Notifications_System.repositories.IMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final IMessageRepository messageRepository;
    private final MessageMapper messageMapper;

    public MessageDto saveAndSendMessage(ChatMessageRequest message, String sender, String receiverId) {

        Message entity = new Message();

        entity.setChatId(message.getChatId());
        entity.setSenderId(sender);
        entity.setReceiverId(receiverId);
        entity.setContent(message.getContent());
        entity.setTimeStamp(LocalDateTime.now());
        entity.setType(message.getType());
        entity.setStatus(MessageStatus.SENT);

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

    public MessageDto markDelivered(String messageId, String receiverId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        if (!receiverId.equals(message.getReceiverId())) {
            throw new RuntimeException("Only receiver can acknowledge delivery");
        }
        if (message.getStatus() == MessageStatus.SENT) {
            message.setStatus(MessageStatus.DELIVERED);
            message = messageRepository.save(message);
        }
        return messageMapper.toDto(message);
    }

    public MessageDto markRead(String messageId, String receiverId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        if (!receiverId.equals(message.getReceiverId())) {
            throw new RuntimeException("Only receiver can acknowledge read");
        }
        if (message.getStatus() != MessageStatus.READ) {
            message.setStatus(MessageStatus.READ);
            message = messageRepository.save(message);
        }
        return messageMapper.toDto(message);
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

    public AllMessagesForChat getAllMessages(String chatId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timeStamp").descending());
        Page<Message> messagePage =
                messageRepository.findByChatIdOrderByTimeStampDesc(chatId, pageable);

        List<MessageDto> messageDtos =
                messageMapper.toDtoList(messagePage.getContent());

        AllMessagesForChat response = new AllMessagesForChat();
        response.setMessageDtoList(messageDtos);
        response.setCurrentPage(messagePage.getNumber());
        response.setTotalPages(messagePage.getTotalPages());
        response.setTotalElements(messagePage.getTotalElements());

        return response;
    }


}
