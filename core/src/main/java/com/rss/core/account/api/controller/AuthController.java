package com.rss.core.account.api.controller;

import com.rss.core.account.application.dto.AuthRequest;
import com.rss.core.account.application.dto.AuthResponse;
import com.rss.core.account.application.dto.RegisterDriverRequest;
import com.rss.core.account.application.dto.RegisterRiderRequest;
import com.rss.core.account.application.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@SecurityRequirements()
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register/rider")
    public ResponseEntity<AuthResponse> registerRider(@RequestBody @Valid RegisterRiderRequest request) {
        return ResponseEntity.ok(authService.registerRider(request));
    }

    @PostMapping("/register/driver")
    public ResponseEntity<AuthResponse> registerDriver(@RequestBody @Valid RegisterDriverRequest request) {
        return ResponseEntity.ok(authService.registerDriver(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody @Valid AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}