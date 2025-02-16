package com.rss.backend.account.service;

import com.rss.backend.account.config.JwtService;
import com.rss.backend.account.dto.AuthRequest;
import com.rss.backend.account.dto.AuthResponse;
import com.rss.backend.account.dto.RegisterDriverRequest;
import com.rss.backend.account.dto.RegisterRiderRequest;
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

    public AuthResponse registerRider(RegisterRiderRequest request) {
        // Create User
        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.RIDER)
                .build();

        // Save User
        userRepository.save(user);

        // Create and Save Rider or Driver
        var rider = Rider.builder()
                .debt(0)
                .user(user)
                .build();
        riderRepository.save(rider);

        // Generate JWT
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthResponse registerDriver(RegisterDriverRequest request) {
        // Create User
        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DRIVER)
                .build();

        // Save User
        userRepository.save(user);

        // Create and Save Rider or Driver
        var driver = Driver.builder()
                .licenseNumber(request.getLicenseNumber())
                .vehicleDetails(request.getVehicleDetails())
                .credit(0)
                .user(user)
                .build();
        driverRepository.save(driver);

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