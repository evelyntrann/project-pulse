package com.projectpulse.auth;

import com.projectpulse.auth.dto.LoginRequest;
import com.projectpulse.auth.dto.LoginResponse;
import com.projectpulse.user.UserEntity;
import com.projectpulse.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest req) {
        UserEntity user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        var userDto = new LoginResponse.UserDto(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole());
        return new LoginResponse(token, userDto);
    }

    public LoginResponse.UserDto me(String userId) {
        UserEntity user = userRepo.findById(Long.parseLong(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new LoginResponse.UserDto(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole());
    }
}
