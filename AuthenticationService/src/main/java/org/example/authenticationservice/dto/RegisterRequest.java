package org.example.authenticationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.example.authenticationservice.entity.Roles;

@Builder
public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        Roles role,
        String firstName,
        String lastName,
        String email,
        String telephone,
        String address
) {}
