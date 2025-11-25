package org.example.monitorigservice.entity;


import jakarta.persistence.*;
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

    @OneToMany(
            mappedBy = "device",
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<MonitoringEntity> consumptionValues;
}
