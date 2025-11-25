package org.example.monitorigservice.repository;

import org.example.monitorigservice.entity.DeviceEntity;
import org.example.monitorigservice.entity.MonitoringEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {
}
