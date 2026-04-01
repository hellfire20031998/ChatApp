package com.hellFire.Real_Time_Notifications_System.services;

import com.hellFire.Real_Time_Notifications_System.dtos.ChatDto;
import com.hellFire.Real_Time_Notifications_System.dtos.UserDto;
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
import java.util.Set;
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


}