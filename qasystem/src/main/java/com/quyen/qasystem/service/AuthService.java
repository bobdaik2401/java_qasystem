package com.quyen.qasystem.service;

import com.quyen.qasystem.dto.LoginRequest;
import com.quyen.qasystem.dto.RegisterRequest;
import com.quyen.qasystem.entity.User;
import com.quyen.qasystem.enums.Role;
import com.quyen.qasystem.repository.UserRepository;
import com.quyen.qasystem.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ================== REGISTER ==================
    public void register(RegisterRequest request) {

        String username = request.getUsername();
        String email = request.getEmail();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .status("ACTIVE")
                .enabled(true)
                .isVerified(true)
                .build();

        userRepository.save(user);
    }

    // ================== LOGIN ==================
    public String login(LoginRequest request) {

        String login = request.getUsernameOrEmail();

        User user = userRepository
                .findForLogin(login)
                .orElseThrow(() -> new BadCredentialsException("USER NOT FOUND"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account disabled");
        }

        if (!user.isVerified()) {
            throw new BadCredentialsException("Account not verified");
        }
        return jwtService.generateToken(user.getUsername());
    }
}
