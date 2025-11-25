package org.example.authenticationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserServiceDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String telephone;
    private String address;
}
