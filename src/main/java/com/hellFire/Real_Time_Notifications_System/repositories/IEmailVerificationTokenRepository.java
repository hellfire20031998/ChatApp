package com.hellFire.Real_Time_Notifications_System.repositories;

import com.hellFire.Real_Time_Notifications_System.models.EmailVerificationToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface IEmailVerificationTokenRepository extends MongoRepository<EmailVerificationToken, String> {
    Optional<EmailVerificationToken> findByTokenHashAndUsedAtIsNull(String tokenHash);
    void deleteByUserId(String userId);
}
