package com.hellFire.Real_Time_Notifications_System.controller;

import com.hellFire.Real_Time_Notifications_System.dtos.MessageDto;
import com.hellFire.Real_Time_Notifications_System.dtos.request.ChatMessageRequest;
import com.hellFire.Real_Time_Notifications_System.services.ChatService;
import com.hellFire.Real_Time_Notifications_System.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

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

        MessageDto savedMessage = messageService.saveAndSendMessage(message, sender);

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