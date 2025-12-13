package org.example.monitorigservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExceededConsumptionDTO {
    private Long userId;
    private Long deviceId;
    private LocalDateTime timestamp;
    private Double actualConsumption;
    private Double limit;
}