package com.rss.backend.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDriverRequest {
    private String username;
    private String email;
    private String password;

    private String licenseNumber;
    private String vehicleDetails;
}