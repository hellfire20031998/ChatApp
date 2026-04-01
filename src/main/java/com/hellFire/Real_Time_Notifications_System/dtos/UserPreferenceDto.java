package com.hellFire.Real_Time_Notifications_System.dtos;

import com.hellFire.Real_Time_Notifications_System.models.enums.ThemeMode;
import com.hellFire.Real_Time_Notifications_System.models.enums.ThemePreset;
import lombok.Data;

@Data
public class UserPreferenceDto {
    private ThemeMode themeMode;
    private ThemePreset themePreset;
}
