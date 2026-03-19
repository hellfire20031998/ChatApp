package com.hellFire.Real_Time_Notifications_System.services;

import com.hellFire.Real_Time_Notifications_System.dtos.request.CreateUserRequest;
import com.hellFire.Real_Time_Notifications_System.dtos.response.UserResponse;
import com.hellFire.Real_Time_Notifications_System.mapper.UserMapper;
import com.hellFire.Real_Time_Notifications_System.models.AppUsers;
import com.hellFire.Real_Time_Notifications_System.repositories.IUserRepository;
import com.hellFire.Real_Time_Notifications_System.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final IUserRepository repo;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;

    public AuthService(IUserRepository repo, JwtUtil jwtUtil, PasswordEncoder encoder, UserMapper userMapper) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
        this.encoder = encoder;
        this.userMapper = userMapper;
    }

    public UserResponse register(CreateUserRequest request) {
        Optional<AppUsers> appUsersOptional = repo.findByUsername(request.getUsername());
        if (appUsersOptional.isPresent()){
            throw new RuntimeException("Username already taken");
        }
        request.setPassword(encoder.encode(request.getPassword()));

        return createUserResponse(repo.save(userMapper.toEntity(request)));
    }

    public UserResponse login(String email, String password) {
        AppUsers user = repo.findByEmail(email)
                .orElseThrow();

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return createUserResponse(user);
    }

    private UserResponse createUserResponse(AppUsers appUsers){
        UserResponse userResponse = new UserResponse();
        userResponse.setUser(userMapper.toDto(appUsers));
        userResponse.setToken(jwtUtil.generateToken(appUsers));
        return userResponse;
    }
}