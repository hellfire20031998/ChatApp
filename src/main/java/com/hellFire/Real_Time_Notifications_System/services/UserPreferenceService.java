package com.hellFire.Real_Time_Notifications_System.services;

import com.hellFire.Real_Time_Notifications_System.dtos.UserPreferenceDto;
import com.hellFire.Real_Time_Notifications_System.dtos.request.UpdateUserPreferenceRequest;
import com.hellFire.Real_Time_Notifications_System.models.AppUsers;
import com.hellFire.Real_Time_Notifications_System.models.UserPreference;
import com.hellFire.Real_Time_Notifications_System.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final IUserRepository userRepository;

    public UserPreferenceDto getPreferences(String userId) {
        AppUsers user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserPreference preference = ensurePreference(user);
        userRepository.save(user);
        return toDto(preference);
    }

    public UserPreferenceDto createOrReplacePreferences(String userId, UpdateUserPreferenceRequest request) {
        AppUsers user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPreference preference = new UserPreference();
        if (request.getThemeMode() != null) {
            preference.setThemeMode(request.getThemeMode());
        }
        if (request.getThemePreset() != null) {
            preference.setThemePreset(request.getThemePreset());
        }

        user.setUserPreference(preference);
        userRepository.save(user);
        return toDto(preference);
    }

    public UserPreferenceDto updatePreferences(String userId, UpdateUserPreferenceRequest request) {
        AppUsers user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPreference preference = ensurePreference(user);
        if (request.getThemeMode() != null) {
            preference.setThemeMode(request.getThemeMode());
        }
        if (request.getThemePreset() != null) {
            preference.setThemePreset(request.getThemePreset());
        }

        user.setUserPreference(preference);
        userRepository.save(user);
        return toDto(preference);
    }

    private UserPreference ensurePreference(AppUsers user) {
        if (user.getUserPreference() == null) {
            user.setUserPreference(new UserPreference());
        }
        return user.getUserPreference();
    }

    private UserPreferenceDto toDto(UserPreference preference) {
        UserPreferenceDto dto = new UserPreferenceDto();
        dto.setThemeMode(preference.getThemeMode());
        dto.setThemePreset(preference.getThemePreset());
        return dto;
    }
}
