package com.hellFire.Real_Time_Notifications_System.dtos;


import lombok.Data;

import java.time.Instant;

@Data
public class BaseEntityDto {
    private String id;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
}
