package com.delivery.user.service;

import com.delivery.user.domain.User;
import com.delivery.user.dto.AuthResponse;
import com.delivery.user.dto.LoginRequest;
import com.delivery.user.dto.RegisterRequest;

import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.delivery.user.repository.RoleRepository;
import com.delivery.user.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtService jwtService, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        User user = User.builder().enabled(true)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .build();
        user.setRoles(Set.of(roleRepository.findByName("ROLE_" + request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found"))));
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return AuthResponse.builder().token(token).build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtService.generateToken(user);
        return AuthResponse.builder().token(token).build();

    }

}
