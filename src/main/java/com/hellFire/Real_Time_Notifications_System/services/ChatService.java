package com.hellFire.Real_Time_Notifications_System.services;

import com.hellFire.Real_Time_Notifications_System.dtos.ChatDto;
import com.hellFire.Real_Time_Notifications_System.dtos.UserDto;
import com.hellFire.Real_Time_Notifications_System.mapper.ChatMapper;
import com.hellFire.Real_Time_Notifications_System.mapper.UserMapper;
import com.hellFire.Real_Time_Notifications_System.models.AppUsers;
import com.hellFire.Real_Time_Notifications_System.models.Chat;
import com.hellFire.Real_Time_Notifications_System.repositories.IChatRepository;
import com.hellFire.Real_Time_Notifications_System.repositories.IUserRepository;
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

        Map<String, UserDto> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(
                        AppUsers::getId,
                        userMapper::toDto
                ));

        return chatMapper.toDto(chat, currentUserId, userMap);
    }

    public List<ChatDto> getUserChats(String userId) {

        List<Chat> chats = chatRepository.findByParticipantIdsContaining(userId);

        Set<String> userIds = chats.stream()
                .flatMap(chat -> chat.getParticipantIds().stream())
                .collect(Collectors.toSet());

        Map<String, UserDto> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(
                        AppUsers::getId,
                        userMapper::toDto
                ));

        return chats.stream()
                .map(chat -> chatMapper.toDto(chat, userId, userMap))
                .toList();
    }
}