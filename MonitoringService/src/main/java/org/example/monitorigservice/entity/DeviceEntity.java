package org.example.monitorigservice.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Builder
@Table
@AllArgsConstructor
@NoArgsConstructor
public class DeviceEntity {

    @Id
    private Long id;

    @Column
    @NotNull
    private Double consumption;

    @Column
    @NotNull
    private Long userId;

    @OneToMany(
            mappedBy = "device",
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<MonitoringEntity> consumptionValues;
}
