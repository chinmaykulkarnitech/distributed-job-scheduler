package com.chinmaytech.job_scheduler_backend.mapper;

import com.chinmaytech.job_scheduler_backend.dto.auth.LoginResponse;
import com.chinmaytech.job_scheduler_backend.dto.auth.RegisterResponse;
import com.chinmaytech.job_scheduler_backend.entity.User;
import org.springframework.stereotype.Component;


@Component
public class AuthMapper {
    public RegisterResponse mapUserToRegisterResponse(User savedUser){
        return RegisterResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    public LoginResponse mapUserToLoginResponse(User user,String token){
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
               .build();
    }

}
