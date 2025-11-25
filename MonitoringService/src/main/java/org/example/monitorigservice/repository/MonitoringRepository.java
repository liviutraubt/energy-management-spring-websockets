package org.example.monitorigservice.repository;

import org.example.monitorigservice.entity.MonitoringEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MonitoringRepository extends JpaRepository<MonitoringEntity, Long> {
    public List<MonitoringEntity> findAllByDeviceId(Long deviceId);
    public MonitoringEntity findByDeviceIdAndTimestamp(Long deviceId, LocalDateTime timestamp);
    public List<MonitoringEntity> findAllByDeviceIdAndTimestampBetweenOrderByTimestampAsc(Long deviceId, LocalDateTime startTimestamp, LocalDateTime endTimestamp);
}
