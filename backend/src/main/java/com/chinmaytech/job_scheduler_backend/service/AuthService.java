package com.chinmaytech.job_scheduler_backend.service;

import com.chinmaytech.job_scheduler_backend.dto.auth.LoginRequest;
import com.chinmaytech.job_scheduler_backend.dto.auth.LoginResponse;
import com.chinmaytech.job_scheduler_backend.dto.auth.RegisterRequest;
import com.chinmaytech.job_scheduler_backend.dto.auth.RegisterResponse;
import com.chinmaytech.job_scheduler_backend.entity.User;
import com.chinmaytech.job_scheduler_backend.exception.BadRequestException;
import com.chinmaytech.job_scheduler_backend.exception.ConflictException;
import com.chinmaytech.job_scheduler_backend.mapper.AuthMapper;
import com.chinmaytech.job_scheduler_backend.repository.UserRepository;
import com.chinmaytech.job_scheduler_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final JwtService jwtService;

    public RegisterResponse register(RegisterRequest request) {

        String email = request.getEmail();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }

        // 2. Create User
        User user = User.builder()
                .name(request.getName())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        // 3. Save User
        User savedUser=userRepository.save(user);

        // 4. Convert Entity → Response DTO
        return authMapper.mapUserToRegisterResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request){

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException("Invalid email or password")
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail()
        );


        return authMapper.mapUserToLoginResponse(user,token);
    }
}