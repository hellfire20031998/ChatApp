package com.hellFire.Real_Time_Notifications_System.dtos.response;

import com.hellFire.Real_Time_Notifications_System.dtos.ChatDto;
import lombok.Data;

import java.util.List;

@Data
public class AllChat {
    List<ChatDto> allChats;
}
