package com.hellFire.Real_Time_Notifications_System.services.cache;

import com.hellFire.Real_Time_Notifications_System.dtos.UserDto;

import java.util.Map;
import java.util.Set;

public interface UserCacheService {
    Map<String, UserDto> getUsersByIds(Set<String> userIds);
    void evictUser(String userId);
}
