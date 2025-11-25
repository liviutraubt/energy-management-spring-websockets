package org.example.monitorigservice.mapper;

import org.example.monitorigservice.dto.MonitoringDTO;
import org.example.monitorigservice.entity.MonitoringEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DeviceMapper.class, MonitoringMapper.class})
public interface MonitoringMapper {
    MonitoringDTO monitoringEntityToMonitoringDTO(MonitoringEntity monitoringEntity);
    MonitoringEntity monitoringDTOToMonitoringEntity(MonitoringDTO monitoringDTO);
    List<MonitoringDTO> monitoringEntityToMonitoringDTO(List<MonitoringEntity> monitoringEntityList);
    List<MonitoringEntity> monitoringDTOToMonitoringEntity(List<MonitoringDTO> monitoringDTOList);
}
