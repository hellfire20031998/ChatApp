package com.hellFire.Real_Time_Notifications_System.dtos.request;

import com.hellFire.Real_Time_Notifications_System.models.enums.ThemeMode;
import com.hellFire.Real_Time_Notifications_System.models.enums.ThemePreset;
import lombok.Data;

@Data
public class UpdateUserPreferenceRequest {
    private ThemeMode themeMode;
    private ThemePreset themePreset;
}
