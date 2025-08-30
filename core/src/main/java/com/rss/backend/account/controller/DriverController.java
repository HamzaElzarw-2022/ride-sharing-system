package com.rss.backend.account.controller;

import com.rss.backend.domain.entity.User;
import com.rss.backend.account.repository.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class DriverController {
    private final UserRepository userRepository;

    @GetMapping("/details/{userId}")
    public String getDriverDetails(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return "Driver: " + user.getUsername() + ", License: " + user.getDriver().getLicenseNumber();
    }

    @GetMapping("/allDetails/")
    public Stream<String> getAllDriverDetails() {
        return userRepository.findAll().stream().map(user -> "Driver: " + user.getUsername() + ", License: " + user.getDriver().getLicenseNumber());
    }
}