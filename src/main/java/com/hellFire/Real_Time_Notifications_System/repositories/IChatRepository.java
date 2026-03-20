package com.hellFire.Real_Time_Notifications_System.repositories;

import com.hellFire.Real_Time_Notifications_System.models.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;


public interface IChatRepository extends MongoRepository<Chat, String> {
    Optional<Chat> findByParticipantIdsContainingAndParticipantIdsContaining(
            String user1, String user2);
    List<Chat> findByParticipantIdsContaining(String userId);
    Optional<Chat> findByChatKey(String chatKey);

}
