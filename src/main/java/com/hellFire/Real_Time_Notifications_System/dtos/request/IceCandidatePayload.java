package com.hellFire.Real_Time_Notifications_System.dtos.request;

import lombok.Data;

@Data
public class IceCandidatePayload {
    private String candidate;
    private String sdpMid;
    private Integer sdpMLineIndex;
}
