package com.hellFire.Real_Time_Notifications_System.dtos.response;

import com.hellFire.Real_Time_Notifications_System.dtos.MessageDto;
import lombok.Data;

import java.util.List;

@Data
public class AllMessagesForChat {
    private List<MessageDto> messageDtoList;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}