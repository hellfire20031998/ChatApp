package com.hellFire.Real_Time_Notifications_System.controller;

import com.hellFire.Real_Time_Notifications_System.dtos.MessageDto;
import com.hellFire.Real_Time_Notifications_System.dtos.request.ChatMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatWebSocketController {

    private static final String USER_MESSAGES_QUEUE = "/queue/messages";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageRequest message, Principal principal) {

        String sender = principal.getName();
        String receiverId = message.getReceiverId();

        MessageDto messageDto = MessageDto.builder()
                .chatId(message.getChatId())
                .senderId(sender)
                .receiverId(receiverId)
                .content(message.getContent())
                .timeStamp(LocalDateTime.now())
                .type(message.getType())
                .build();

        messagingTemplate.convertAndSendToUser(receiverId, USER_MESSAGES_QUEUE, messageDto);
        if (!sender.equals(receiverId)) {
            messagingTemplate.convertAndSendToUser(sender, USER_MESSAGES_QUEUE, messageDto);
        }
    }
}