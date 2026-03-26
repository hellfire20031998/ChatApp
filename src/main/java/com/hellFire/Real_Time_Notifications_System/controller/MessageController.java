package com.hellFire.Real_Time_Notifications_System.controller;

import com.hellFire.Real_Time_Notifications_System.dtos.MessageDto;
import com.hellFire.Real_Time_Notifications_System.dtos.request.UpdateMessageRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.response.ApiResponse;
import com.hellFire.Real_Time_Notifications_System.models.AppUsers;
import com.hellFire.Real_Time_Notifications_System.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PatchMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageDto>> updateMessage(
            @PathVariable String messageId,
            @RequestBody UpdateMessageRequest request,
            @AuthenticationPrincipal AppUsers currentUser
    ) {
        MessageDto updatedMessage = messageService.updateMessage(messageId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(updatedMessage));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageDto>> deleteMessage(
            @PathVariable String messageId,
            @AuthenticationPrincipal AppUsers currentUser
    ) {
        MessageDto deletedMessage = messageService.deleteMessage(messageId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(deletedMessage));
    }
}
