package com.hellFire.Real_Time_Notifications_System.services;

import com.hellFire.Real_Time_Notifications_System.dtos.ChatDto;
import com.hellFire.Real_Time_Notifications_System.dtos.UserDto;
import com.hellFire.Real_Time_Notifications_System.dtos.request.CreateGroupRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.request.ModifyGroupMembersRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.request.RenameGroupRequest;
import com.hellFire.Real_Time_Notifications_System.mapper.ChatMapper;
import com.hellFire.Real_Time_Notifications_System.mapper.UserMapper;
import com.hellFire.Real_Time_Notifications_System.models.Chat;
import com.hellFire.Real_Time_Notifications_System.models.Message;
import com.hellFire.Real_Time_Notifications_System.models.enums.MessageStatus;
import com.hellFire.Real_Time_Notifications_System.repositories.IChatRepository;
import com.hellFire.Real_Time_Notifications_System.repositories.IMessageRepository;
import com.hellFire.Real_Time_Notifications_System.repositories.IUserRepository;
import com.hellFire.Real_Time_Notifications_System.services.cache.UserCacheService;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final IChatRepository chatRepository;
    private final IUserRepository userRepository;
    private final UserMapper userMapper;
    private final ChatMapper chatMapper;
    private final UserCacheService userCacheService;
    private final IMessageRepository messageRepository;


    public List<UserDto> searchUsers(String query, String currentUserId) {
        return userMapper.toDtoList( userRepository
                .findByEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(query, query)
                .stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .toList());


    }

    public ChatDto getOrCreateChat(String currentUserId, String targetUserId) {

        String chatKey = Stream.of(currentUserId, targetUserId)
                .sorted()
                .collect(Collectors.joining("_"));

        Optional<Chat> existingChat = chatRepository.findByChatKey(chatKey);

        Chat chat;

        if (existingChat.isPresent()) {
            chat = existingChat.get();
        } else {
            chat = new Chat();
            chat.setChatKey(chatKey);
            chat.setParticipantIds(Set.of(currentUserId, targetUserId));
            chat.setGroup(false);

            try {
                chat = chatRepository.save(chat);
            } catch (DuplicateKeyException e) {
                chat = chatRepository.findByChatKey(chatKey)
                        .orElseThrow(() -> new RuntimeException("Chat not found"));
            }
        }

        Set<String> userIds = chat.getParticipantIds();

        Map<String, UserDto> userMap = userCacheService.getUsersByIds(userIds);

        Message last = messageRepository.findTopByChatIdOrderByTimeStampDesc(chat.getId());
        String lastMessage = null;
        java.time.LocalDateTime lastMessageTime = null;
        if (last != null) {
            lastMessage = last.isDeleted() ? "Message deleted" : last.getContent();
            lastMessageTime = last.getTimeStamp();
        }
        return chatMapper.toDto(chat, currentUserId, userMap, lastMessage, lastMessageTime, 0);
    }

    public ChatDto createGroup(String currentUserId, CreateGroupRequest request) {
        String groupName = request.getGroupName() == null ? "" : request.getGroupName().trim();
        if (groupName.isEmpty()) {
            throw new RuntimeException("Group name is required");
        }
        Set<String> participantIds = new HashSet<>(
                request.getParticipantIds() == null ? List.of() : request.getParticipantIds()
        );
        participantIds.removeIf(id -> id == null || id.isBlank());
        participantIds.add(currentUserId);
        if (participantIds.size() < 2) {
            throw new RuntimeException("Add at least one other member");
        }
        long existingUsers = userRepository.findAllById(participantIds).stream().count();
        if (existingUsers != participantIds.size()) {
            throw new RuntimeException("One or more selected users do not exist");
        }

        Chat groupChat = new Chat();
        groupChat.setGroup(true);
        groupChat.setGroupName(groupName);
        groupChat.setParticipantIds(participantIds);
        groupChat.setCreatedBy(currentUserId);
        groupChat.setAdminIds(new HashSet<>(Set.of(currentUserId)));
        groupChat.setChatKey("group_" + UUID.randomUUID());
        Chat saved = chatRepository.save(groupChat);

        Map<String, UserDto> userMap = userCacheService.getUsersByIds(participantIds);
        return chatMapper.toDto(saved, currentUserId, userMap, null, null, 0);
    }

    public ChatDto renameGroup(String currentUserId, String chatId, RenameGroupRequest request) {
        Chat chat = getChatForUser(chatId, currentUserId);
        if (!chat.isGroup()) throw new RuntimeException("Not a group chat");
        requireGroupAdmin(chat, currentUserId);
        String name = request.getGroupName() == null ? "" : request.getGroupName().trim();
        if (name.isEmpty()) throw new RuntimeException("Group name is required");
        chat.setGroupName(name);
        Chat saved = chatRepository.save(chat);
        Map<String, UserDto> userMap = userCacheService.getUsersByIds(saved.getParticipantIds());
        Message last = messageRepository.findTopByChatIdOrderByTimeStampDesc(saved.getId());
        String lastMessage = last == null ? null : (last.isDeleted() ? "Message deleted" : last.getContent());
        java.time.LocalDateTime lastMessageTime = last == null ? null : last.getTimeStamp();
        long unread = messageRepository.countByChatIdAndReceiverIdAndStatusNotAndDeletedFalse(
                saved.getId(), currentUserId, MessageStatus.READ
        );
        return chatMapper.toDto(saved, currentUserId, userMap, lastMessage, lastMessageTime, (int) unread);
    }

    public ChatDto addMembers(String currentUserId, String chatId, ModifyGroupMembersRequest request) {
        Chat chat = getChatForUser(chatId, currentUserId);
        if (!chat.isGroup()) throw new RuntimeException("Not a group chat");
        requireGroupAdmin(chat, currentUserId);
        Set<String> additions = new HashSet<>(request.getParticipantIds() == null ? List.of() : request.getParticipantIds());
        additions.removeIf(id -> id == null || id.isBlank());
        additions.remove(currentUserId);
        if (additions.isEmpty()) throw new RuntimeException("No valid members to add");
        long existingUsers = userRepository.findAllById(additions).stream().count();
        if (existingUsers != additions.size()) throw new RuntimeException("One or more selected users do not exist");
        Set<String> participants = new HashSet<>(chat.getParticipantIds());
        participants.addAll(additions);
        chat.setParticipantIds(participants);
        Chat saved = chatRepository.save(chat);
        Map<String, UserDto> userMap = userCacheService.getUsersByIds(saved.getParticipantIds());
        Message last = messageRepository.findTopByChatIdOrderByTimeStampDesc(saved.getId());
        String lastMessage = last == null ? null : (last.isDeleted() ? "Message deleted" : last.getContent());
        java.time.LocalDateTime lastMessageTime = last == null ? null : last.getTimeStamp();
        long unread = messageRepository.countByChatIdAndReceiverIdAndStatusNotAndDeletedFalse(
                saved.getId(), currentUserId, MessageStatus.READ
        );
        return chatMapper.toDto(saved, currentUserId, userMap, lastMessage, lastMessageTime, (int) unread);
    }

    public ChatDto removeMember(String currentUserId, String chatId, String memberId) {
        Chat chat = getChatForUser(chatId, currentUserId);
        if (!chat.isGroup()) throw new RuntimeException("Not a group chat");
        requireGroupAdmin(chat, currentUserId);
        if (memberId == null || memberId.isBlank()) throw new RuntimeException("memberId is required");
        if (memberId.equals(currentUserId)) {
            throw new RuntimeException("Use leave group to remove yourself");
        }
        Set<String> participants = new HashSet<>(chat.getParticipantIds());
        if (!participants.contains(memberId)) throw new RuntimeException("User is not a group member");
        if (participants.size() <= 2) throw new RuntimeException("Group must have at least 2 members");
        participants.remove(memberId);
        chat.setParticipantIds(participants);
        Set<String> adminIds = normalizedAdmins(chat);
        adminIds.remove(memberId);
        if (adminIds.isEmpty()) {
            adminIds.add(currentUserId);
        }
        chat.setAdminIds(adminIds);
        Chat saved = chatRepository.save(chat);
        Map<String, UserDto> userMap = userCacheService.getUsersByIds(saved.getParticipantIds());
        Message last = messageRepository.findTopByChatIdOrderByTimeStampDesc(saved.getId());
        String lastMessage = last == null ? null : (last.isDeleted() ? "Message deleted" : last.getContent());
        java.time.LocalDateTime lastMessageTime = last == null ? null : last.getTimeStamp();
        long unread = messageRepository.countByChatIdAndReceiverIdAndStatusNotAndDeletedFalse(
                saved.getId(), currentUserId, MessageStatus.READ
        );
        return chatMapper.toDto(saved, currentUserId, userMap, lastMessage, lastMessageTime, (int) unread);
    }

    public void leaveGroup(String currentUserId, String chatId) {
        Chat chat = getChatForUser(chatId, currentUserId);
        if (!chat.isGroup()) throw new RuntimeException("Not a group chat");
        Set<String> participants = new HashSet<>(chat.getParticipantIds());
        if (participants.size() <= 2) throw new RuntimeException("Group must have at least 2 members");
        participants.remove(currentUserId);
        chat.setParticipantIds(participants);
        Set<String> adminIds = normalizedAdmins(chat);
        adminIds.remove(currentUserId);
        if (adminIds.isEmpty()) {
            Iterator<String> it = participants.iterator();
            if (it.hasNext()) adminIds.add(it.next());
        }
        chat.setAdminIds(adminIds);
        chatRepository.save(chat);
    }

    public List<ChatDto> getUserChats(String userId) {

        List<Chat> chats = chatRepository.findByParticipantIdsContaining(userId);

        Set<String> userIds = chats.stream()
                .flatMap(chat -> chat.getParticipantIds().stream())
                .collect(Collectors.toSet());

        Map<String, UserDto> userMap = userCacheService.getUsersByIds(userIds);

        return chats.stream()
                .map(chat -> {
                    Message last = messageRepository.findTopByChatIdOrderByTimeStampDesc(chat.getId());
                    String lastMessage = null;
                    java.time.LocalDateTime lastMessageTime = null;
                    if (last != null) {
                        lastMessage = last.isDeleted() ? "Message deleted" : last.getContent();
                        lastMessageTime = last.getTimeStamp();
                    }
                    long unread = messageRepository.countByChatIdAndReceiverIdAndStatusNotAndDeletedFalse(
                            chat.getId(),
                            userId,
                            MessageStatus.READ
                    );
                    return chatMapper.toDto(
                            chat,
                            userId,
                            userMap,
                            lastMessage,
                            lastMessageTime,
                            (int) unread
                    );
                })
                .sorted((a, b) -> {
                    java.time.LocalDateTime ta = a.getLastMessageTime();
                    java.time.LocalDateTime tb = b.getLastMessageTime();
                    if (ta == null && tb == null) return 0;
                    if (ta == null) return 1;
                    if (tb == null) return -1;
                    return tb.compareTo(ta);
                })
                .toList();
    }

    public Chat getChatForUser(String chatId, String userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        if (chat.getParticipantIds() == null || !chat.getParticipantIds().contains(userId)) {
            throw new RuntimeException("You are not part of this chat");
        }
        ensureAdminMetadata(chat);
        return chat;
    }

    private void requireGroupAdmin(Chat chat, String userId) {
        Set<String> admins = normalizedAdmins(chat);
        if (!admins.contains(userId)) {
            throw new RuntimeException("Only group admins can perform this action");
        }
    }

    private Set<String> normalizedAdmins(Chat chat) {
        ensureAdminMetadata(chat);
        return new HashSet<>(chat.getAdminIds());
    }

    private void ensureAdminMetadata(Chat chat) {
        boolean changed = false;
        if (chat.getParticipantIds() == null || chat.getParticipantIds().isEmpty()) return;
        if (chat.getCreatedBy() == null || chat.getCreatedBy().isBlank() || !chat.getParticipantIds().contains(chat.getCreatedBy())) {
            chat.setCreatedBy(chat.getParticipantIds().iterator().next());
            changed = true;
        }
        Set<String> admins = chat.getAdminIds() == null ? new HashSet<>() : new HashSet<>(chat.getAdminIds());
        admins.removeIf(id -> id == null || id.isBlank() || !chat.getParticipantIds().contains(id));
        if (admins.isEmpty()) {
            admins.add(chat.getCreatedBy());
        }
        if (chat.getAdminIds() == null || !chat.getAdminIds().equals(admins)) {
            chat.setAdminIds(admins);
            changed = true;
        }
        if (changed) {
            chatRepository.save(chat);
        }
    }


}