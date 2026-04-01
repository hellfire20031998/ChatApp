package com.hellFire.Real_Time_Notifications_System.models;

import com.hellFire.Real_Time_Notifications_System.models.enums.ThemeMode;
import com.hellFire.Real_Time_Notifications_System.models.enums.ThemePreset;
import lombok.Data;

@Data
public class UserPreference {
    private ThemeMode themeMode = ThemeMode.SYSTEM;
    private ThemePreset themePreset = ThemePreset.EMERALD;
}
