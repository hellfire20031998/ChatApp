package com.hellFire.Real_Time_Notifications_System.dtos;

import com.hellFire.Real_Time_Notifications_System.dtos.request.IceCandidatePayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallSignalDto {
    @Builder.Default
    private String eventType = "CALL";
    private String action;
    private String chatId;
    private String callId;
    private String mediaType;
    private String fromUserId;
    private String sdp;
    private IceCandidatePayload iceCandidate;
}
