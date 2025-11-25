package org.example.monitorigservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.monitorigservice.dto.DeviceDTO;
import org.example.monitorigservice.dto.MonitoringRequestDTO;
import org.example.monitorigservice.service.MonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController

@RequiredArgsConstructor
@RequestMapping("/api/monitoring")
public class MonitoringController {
    private final MonitoringService monitoringService;

    @PostMapping("/device")
    public ResponseEntity<?> insertDevice(@RequestBody DeviceDTO deviceDTO){
        try{
            return ResponseEntity.ok(monitoringService.insertDevice(deviceDTO));
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/device/{id}")
    public ResponseEntity<?> deleteDevice(@PathVariable Long id){
        try{
            monitoringService.deleteDevice(id);
            return ResponseEntity.ok().build();
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getMonitoringForDeviceAndDate(@RequestParam("deviceId") Long deviceId, @RequestParam("date") LocalDate date) {
        try{
            return ResponseEntity.ok(monitoringService.getAllForDeviceAndTimestampMonitoring(deviceId, date));
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
