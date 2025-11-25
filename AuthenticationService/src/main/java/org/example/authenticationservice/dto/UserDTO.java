package org.example.authenticationservice.dto;

import lombok.Builder;
import org.example.authenticationservice.entity.Roles;

@Builder
public record UserDTO (Long id, String username, Roles role){}
