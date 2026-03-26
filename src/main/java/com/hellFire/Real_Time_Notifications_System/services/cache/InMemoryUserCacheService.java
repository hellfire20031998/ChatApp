package com.hellFire.Real_Time_Notifications_System.services.cache;

import com.hellFire.Real_Time_Notifications_System.dtos.UserDto;
import com.hellFire.Real_Time_Notifications_System.mapper.UserMapper;
import com.hellFire.Real_Time_Notifications_System.models.AppUsers;
import com.hellFire.Real_Time_Notifications_System.repositories.IUserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class InMemoryUserCacheService implements UserCacheService {

    private final IUserRepository userRepository;
    private final UserMapper userMapper;
    private final ConcurrentHashMap<String, UserDto> cache = new ConcurrentHashMap<>();

    public InMemoryUserCacheService(IUserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Map<String, UserDto> getUsersByIds(Set<String> userIds) {
        Map<String, UserDto> result = new HashMap<>();

        for (String userId : userIds) {
            UserDto cached = cache.get(userId);
            if (cached != null) {
                result.put(userId, cached);
            }
        }

        Set<String> missingUserIds = userIds.stream()
                .filter(userId -> !result.containsKey(userId))
                .collect(Collectors.toSet());

        if (!missingUserIds.isEmpty()) {
            Map<String, UserDto> fetchedUsers = userRepository.findAllById(missingUserIds)
                    .stream()
                    .collect(Collectors.toMap(
                            AppUsers::getId,
                            userMapper::toDto
                    ));

            cache.putAll(fetchedUsers);
            result.putAll(fetchedUsers);
        }

        return result;
    }

    @Override
    public void evictUser(String userId) {
        cache.remove(userId);
    }
}
