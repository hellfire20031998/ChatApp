package com.hellFire.Real_Time_Notifications_System.dtos.request;

import lombok.Data;

@Data
public class CallSignalRequest {
    /** INVITE, ACCEPT, REJECT, CANCEL, OFFER, ANSWER, ICE, HANGUP */
    private String action;
    private String chatId;
    private String callId;
    /** AUDIO or VIDEO */
    private String mediaType;
    private String sdp;
    private IceCandidatePayload iceCandidate;
}
