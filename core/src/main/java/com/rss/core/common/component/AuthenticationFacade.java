package com.rss.core.common.component;

import com.rss.core.account.application.port.out.JwtService;
import com.rss.core.account.domain.repository.UserRepository;
import com.rss.core.account.infrastructure.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationFacade {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public Long getAuthenticatedRiderId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            System.out.println("Unauthenticated: principal not found");
            throw new AuthenticationCredentialsNotFoundException("Unauthenticated: principal not found");
        }
        return principal.getRiderId();
    }

    public Long getAuthenticatedRiderId(String token) {
        String email = jwtService.extractUsername(token);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Unauthenticated: email not found"));
        if (user.getRider() == null) {
            throw new AuthenticationCredentialsNotFoundException("Unauthenticated: rider not found");
        }
        return user.getRider().getId();
    }
}

