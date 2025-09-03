package com.rss.backend.account.application.dto;

import com.rss.backend.account.domain.entity.Role;
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