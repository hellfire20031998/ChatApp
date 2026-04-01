package com.hellFire.Real_Time_Notifications_System.controller;

import com.hellFire.Real_Time_Notifications_System.dtos.MessageDto;
import com.hellFire.Real_Time_Notifications_System.dtos.TypingEventDto;
import com.hellFire.Real_Time_Notifications_System.dtos.request.ChatMessageRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.request.MessageReceiptRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.request.TypingEventRequest;
import com.hellFire.Real_Time_Notifications_System.models.Chat;
import com.hellFire.Real_Time_Notifications_System.services.ChatService;
import com.hellFire.Real_Time_Notifications_System.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final MessageService messageService;
    private static final String USER_MESSAGES_QUEUE = "/queue/messages";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageRequest message, Principal principal) {

        String sender = principal.getName();
        Chat chat = chatService.getChatForUser(message.getChatId(), sender);
        boolean isGroup = chat.isGroup();
        String receiverId = isGroup
                ? null
                : (message.getReceiverId() != null
                    ? message.getReceiverId()
                    : chat.getParticipantIds()
                        .stream()
                        .filter(id -> !id.equals(sender))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Receiver not found")));

        MessageDto savedMessage = messageService.saveAndSendMessage(message, sender, receiverId);

        if (isGroup) {
            Set<String> participants = chat.getParticipantIds();
            for (String participantId : participants) {
                messagingTemplate.convertAndSendToUser(
                        participantId,
                        USER_MESSAGES_QUEUE,
                        savedMessage
                );
            }
        } else {
            messagingTemplate.convertAndSendToUser(
                    savedMessage.getReceiverId(),
                    USER_MESSAGES_QUEUE,
                    savedMessage
            );
            if (!sender.equals(savedMessage.getReceiverId())) {
                messagingTemplate.convertAndSendToUser(
                        sender,
                        USER_MESSAGES_QUEUE,
                        savedMessage
                );
            }
        }
    }

    @MessageMapping("/chat.delivered")
    public void markDelivered(MessageReceiptRequest request, Principal principal) {
        String receiverId = principal.getName();
        MessageDto updated = messageService.markDelivered(request.getMessageId(), receiverId);
        messagingTemplate.convertAndSendToUser(updated.getSenderId(), USER_MESSAGES_QUEUE, updated);
        if (!updated.getSenderId().equals(updated.getReceiverId())) {
            messagingTemplate.convertAndSendToUser(updated.getReceiverId(), USER_MESSAGES_QUEUE, updated);
        }
    }

    @MessageMapping("/chat.read")
    public void markRead(MessageReceiptRequest request, Principal principal) {
        String receiverId = principal.getName();
        MessageDto updated = messageService.markRead(request.getMessageId(), receiverId);
        messagingTemplate.convertAndSendToUser(updated.getSenderId(), USER_MESSAGES_QUEUE, updated);
        if (!updated.getSenderId().equals(updated.getReceiverId())) {
            messagingTemplate.convertAndSendToUser(updated.getReceiverId(), USER_MESSAGES_QUEUE, updated);
        }
    }

    @MessageMapping("/chat.typing")
    public void typing(TypingEventRequest request, Principal principal) {
        String sender = principal.getName();
        Chat chat = chatService.getChatForUser(request.getChatId(), sender);
        TypingEventDto event = TypingEventDto.builder()
                .eventType("TYPING")
                .chatId(request.getChatId())
                .senderId(sender)
                .receiverId(request.getReceiverId())
                .typing(request.isTyping())
                .build();
        if (chat.isGroup()) {
            for (String participantId : chat.getParticipantIds()) {
                if (sender.equals(participantId)) continue;
                messagingTemplate.convertAndSendToUser(participantId, USER_MESSAGES_QUEUE, event);
            }
        } else {
            String receiverId = request.getReceiverId() != null
                    ? request.getReceiverId()
                    : chat.getParticipantIds()
                        .stream()
                        .filter(id -> !id.equals(sender))
                        .findFirst()
                        .orElse(null);
            if (receiverId != null) {
                messagingTemplate.convertAndSendToUser(receiverId, USER_MESSAGES_QUEUE, event);
            }
        }
    }
}