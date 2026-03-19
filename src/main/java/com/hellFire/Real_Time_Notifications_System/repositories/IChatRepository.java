package com.hellFire.Real_Time_Notifications_System.repositories;

import com.hellFire.Real_Time_Notifications_System.models.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface IChatRepository extends MongoRepository<Chat, String> {
}
