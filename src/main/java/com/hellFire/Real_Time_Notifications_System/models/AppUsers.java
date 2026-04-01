package com.hellFire.Real_Time_Notifications_System.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hellFire.Real_Time_Notifications_System.models.UserPreference;
import com.hellFire.Real_Time_Notifications_System.models.enums.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@EqualsAndHashCode(callSuper = true)
@Document(collection = "users")
@Data
public class AppUsers extends BaseEntity {

    private String name;

    @Indexed(unique = true)
    private String email;

    @JsonIgnore
    private String password;

    @Indexed(unique = true)
    private String username;

    private UserRole userRole;
    private UserPreference userPreference = new UserPreference();
}
