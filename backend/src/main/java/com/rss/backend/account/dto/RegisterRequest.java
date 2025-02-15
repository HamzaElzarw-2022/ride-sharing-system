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
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private Role role;

    // Rider-specific fields
    private String preferredPaymentMethod;

    // Driver-specific fields
    private String licenseNumber;
    private String vehicleDetails;
}