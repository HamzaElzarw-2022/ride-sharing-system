package com.rss.backend.account.service;

import com.rss.backend.account.config.JwtService;
import com.rss.backend.account.dto.AuthRequest;
import com.rss.backend.account.dto.AuthResponse;
import com.rss.backend.account.dto.RegisterRequest;
import com.rss.backend.domain.*;
import com.rss.backend.account.repository.DriverRepository;
import com.rss.backend.account.repository.RiderRepository;
import com.rss.backend.account.repository.UserRepository;
import com.rss.backend.domain.entity.Driver;
import com.rss.backend.domain.entity.Rider;
import com.rss.backend.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RiderRepository riderRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // Create User
        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        // Save User
        userRepository.save(user);

        // Create and Save Rider or Driver
        if (request.getRole() == Role.RIDER) {
            var rider = Rider.builder()
                    .preferredPaymentMethod(request.getPreferredPaymentMethod())
                    .user(user)
                    .build();
            riderRepository.save(rider);
        } else if (request.getRole() == Role.DRIVER) {
            var driver = Driver.builder()
                    .licenseNumber(request.getLicenseNumber())
                    .vehicleDetails(request.getVehicleDetails())
                    .user(user)
                    .build();
            driverRepository.save(driver);
        }

        // Generate JWT
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

}