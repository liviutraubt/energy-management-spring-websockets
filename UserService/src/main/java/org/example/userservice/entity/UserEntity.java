package org.example.userservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@Table
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    private Long id;

    @Column
    @NotNull
    private String firstName;
    @Column
    @NotNull
    private String lastName;

    @Column
    @NotNull
    private String email;
    @Column
    @NotNull
    private String telephone;
    @Column
    @NotNull
    private String address;

}
