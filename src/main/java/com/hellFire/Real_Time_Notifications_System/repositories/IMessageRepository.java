package com.hellFire.Real_Time_Notifications_System.repositories;

import com.hellFire.Real_Time_Notifications_System.models.Message;
import com.hellFire.Real_Time_Notifications_System.models.enums.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IMessageRepository extends MongoRepository<Message, String> {
    Page<Message> findByChatIdOrderByTimeStampDesc(String chatId, Pageable pageable);
    Message findTopByChatIdOrderByTimeStampDesc(String chatId);
    long countByChatIdAndReceiverIdAndStatusNotAndDeletedFalse(
            String chatId,
            String receiverId,
            MessageStatus status
    );
}
