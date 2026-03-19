package com.hellFire.Real_Time_Notifications_System.services;

import com.hellFire.Real_Time_Notifications_System.models.AppUsers;
import com.hellFire.Real_Time_Notifications_System.repositories.IUserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final IUserRepository repo;

    public CustomUserDetailsService(IUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        AppUsers user = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getUserRole().name())
                .build();
    }
}