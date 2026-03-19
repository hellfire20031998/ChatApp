package com.hellFire.Real_Time_Notifications_System.dtos.response;

import com.hellFire.Real_Time_Notifications_System.dtos.UserDto;
import lombok.Data;

@Data
public class UserResponse {
    private String token;
    private UserDto user;
}
