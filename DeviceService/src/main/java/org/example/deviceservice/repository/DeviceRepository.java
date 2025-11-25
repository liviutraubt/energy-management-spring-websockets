package org.example.deviceservice.repository;

import org.example.deviceservice.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {
    public List<DeviceEntity> findByUserId(Long userId);
}
