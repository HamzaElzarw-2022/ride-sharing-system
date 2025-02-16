package com.rss.backend.account.dto;

import com.rss.backend.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRiderRequest {
    private String username;
    private String email;
    private String password;
    private Role role;

    private String preferredPaymentMethod;
}