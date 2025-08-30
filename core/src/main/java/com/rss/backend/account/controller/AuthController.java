package com.rss.backend.account.controller;

import com.rss.backend.account.dto.AuthRequest;
import com.rss.backend.account.dto.AuthResponse;
import com.rss.backend.account.dto.RegisterDriverRequest;
import com.rss.backend.account.dto.RegisterRiderRequest;
import com.rss.backend.account.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register/rider")
    public ResponseEntity<AuthResponse> registerRider(@RequestBody RegisterRiderRequest request) {
        return ResponseEntity.ok(authService.registerRider(request));
    }

    @PostMapping("/register/driver")
    public ResponseEntity<AuthResponse> registerDriver(@RequestBody RegisterDriverRequest request) {
        return ResponseEntity.ok(authService.registerDriver(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}