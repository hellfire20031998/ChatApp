package com.hellFire.Real_Time_Notifications_System.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {

    @MessageMapping("/sendMessage")
    @SendTo("/topic/notification")
    public String sendMessage(String message){
        System.out.println("message "+ message);
        return message;
    }
}
