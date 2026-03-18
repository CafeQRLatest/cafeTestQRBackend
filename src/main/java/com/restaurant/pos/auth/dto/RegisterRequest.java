package com.restaurant.pos.auth.dto;

import com.restaurant.pos.auth.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;
    private UUID clientId; // Allow explicit client ID during registration, e.g. for creating new restaurants
    private String country;
    private String posType;
    private String otp;
}
