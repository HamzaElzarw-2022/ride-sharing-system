package com.rss.core.account.api.controller;

import com.rss.core.account.domain.entity.User;
import com.rss.core.account.domain.repository.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rider")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class RiderController {
    private final UserRepository userRepository;

    @GetMapping("/details/{userId}")
    public String getRiderDetails(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return "Rider: " + user.getUsername() + ", Payment Method: " + user.getRider().getDebt();
    }
}