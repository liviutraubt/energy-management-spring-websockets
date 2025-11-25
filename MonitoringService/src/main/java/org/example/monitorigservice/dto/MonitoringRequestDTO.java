package org.example.monitorigservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MonitoringRequestDTO {
    private Long deviceId;
    private LocalDate date;
}
