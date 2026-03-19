package com.hellFire.Real_Time_Notifications_System.repositories;

import com.hellFire.Real_Time_Notifications_System.models.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IMessageRepository extends MongoRepository<Message, String> {
}
