package com.hellFire.Real_Time_Notifications_System.repositories;

import com.hellFire.Real_Time_Notifications_System.models.AppUsers;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface IUserRepository extends MongoRepository<AppUsers, String> {
    Optional<AppUsers> findByEmail(String email);
    Optional<AppUsers> findByUsername(String username);

}
