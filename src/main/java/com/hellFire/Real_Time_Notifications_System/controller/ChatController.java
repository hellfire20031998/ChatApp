package com.hellFire.Real_Time_Notifications_System.controller;

import com.hellFire.Real_Time_Notifications_System.dtos.ChatDto;
import com.hellFire.Real_Time_Notifications_System.dtos.UserDto;
import com.hellFire.Real_Time_Notifications_System.dtos.request.CreateGroupRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.request.ModifyGroupMembersRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.request.RenameGroupRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.request.StartChatRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.response.AllChat;
import com.hellFire.Real_Time_Notifications_System.dtos.response.ApiResponse;
import com.hellFire.Real_Time_Notifications_System.models.AppUsers;
import com.hellFire.Real_Time_Notifications_System.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserDto>>> searchUsers(
            @RequestParam String q,
            Authentication authentication
    ) {
        AppUsers currentUser = (AppUsers) authentication.getPrincipal();

        return ResponseEntity.ok(
                ApiResponse.success(chatService.searchUsers(q, currentUser.getId()))
        );
    }
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<ChatDto>> startChat(
            @RequestBody StartChatRequest request,
            @AuthenticationPrincipal AppUsers currentUser
    ) {
        ChatDto chat = chatService.getOrCreateChat(currentUser.getId(), request.getUserId());

        return ResponseEntity.ok(ApiResponse.success(chat));
    }

    @PostMapping("/group")
    public ResponseEntity<ApiResponse<ChatDto>> createGroup(
            @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal AppUsers currentUser
    ) {
        ChatDto chat = chatService.createGroup(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(chat));
    }

    @PatchMapping("/{chatId}/group")
    public ResponseEntity<ApiResponse<ChatDto>> renameGroup(
            @PathVariable String chatId,
            @RequestBody RenameGroupRequest request,
            @AuthenticationPrincipal AppUsers currentUser
    ) {
        ChatDto chat = chatService.renameGroup(currentUser.getId(), chatId, request);
        return ResponseEntity.ok(ApiResponse.success(chat));
    }

    @PostMapping("/{chatId}/participants")
    public ResponseEntity<ApiResponse<ChatDto>> addMembers(
            @PathVariable String chatId,
            @RequestBody ModifyGroupMembersRequest request,
            @AuthenticationPrincipal AppUsers currentUser
    ) {
        ChatDto chat = chatService.addMembers(currentUser.getId(), chatId, request);
        return ResponseEntity.ok(ApiResponse.success(chat));
    }

    @DeleteMapping("/{chatId}/participants/{memberId}")
    public ResponseEntity<ApiResponse<ChatDto>> removeMember(
            @PathVariable String chatId,
            @PathVariable String memberId,
            @AuthenticationPrincipal AppUsers currentUser
    ) {
        ChatDto chat = chatService.removeMember(currentUser.getId(), chatId, memberId);
        return ResponseEntity.ok(ApiResponse.success(chat));
    }

    @PostMapping("/{chatId}/leave")
    public ResponseEntity<ApiResponse<String>> leaveGroup(
            @PathVariable String chatId,
            @AuthenticationPrincipal AppUsers currentUser
    ) {
        chatService.leaveGroup(currentUser.getId(), chatId);
        return ResponseEntity.ok(ApiResponse.success("Left group"));
    }

    @GetMapping("my-chats")
    public ResponseEntity<ApiResponse<AllChat>> getChats(
            @AuthenticationPrincipal AppUsers currentUser
    ) {
        List<ChatDto> chats = chatService.getUserChats(currentUser.getId());
        AllChat allChat = new AllChat();
        allChat.setAllChats(chats);
        return ResponseEntity.ok(ApiResponse.success(allChat));
    }
}