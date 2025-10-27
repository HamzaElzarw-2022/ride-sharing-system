package com.rss.core.common.component;

import com.rss.core.account.infrastructure.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade {

    public Long getAuthenticatedRiderId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            System.out.println("Unauthenticated: principal not found");
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Unauthenticated: principal not found");
        }
        return principal.getRiderId();
    }
}

