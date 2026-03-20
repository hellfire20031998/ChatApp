package com.hellFire.Real_Time_Notifications_System.mapper;

import com.hellFire.Real_Time_Notifications_System.dtos.UserDto;
import com.hellFire.Real_Time_Notifications_System.dtos.request.CreateUserRequest;
import com.hellFire.Real_Time_Notifications_System.models.AppUsers;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserMapper {

    public UserDto toDto(AppUsers user){
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setDeleted(user.isDeleted());

        return dto;
    }

    public List<UserDto> toDtoList(List<AppUsers> users) {
        if (users == null || users.isEmpty()) return Collections.emptyList();

        return users.stream()
                .map(this::toDto)
                .toList();
    }

    public AppUsers toEntity(CreateUserRequest request){
        if (request == null) return null;

        AppUsers user = new AppUsers();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setUsername(request.getUsername());

        return user;
    }
}
