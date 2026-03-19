package com.hellFire.Real_Time_Notifications_System.dtos;

import com.hellFire.Real_Time_Notifications_System.models.enums.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserDto extends BaseEntityDto {

    private String name;
    private String email;
    private String username;
    private UserRole userRole;
}
