package org.example.monitorigservice.mapper;

import org.example.monitorigservice.dto.DeviceDTO;
import org.example.monitorigservice.entity.DeviceEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DeviceMapper.class, MonitoringMapper.class})
public interface DeviceMapper {
    DeviceDTO deviceEntityToDeviceDTO(DeviceEntity deviceEntity);
    DeviceEntity deviceDTOToDeviceEntity(DeviceDTO deviceDTO);
    List<DeviceDTO> deviceEntityToDeviceDTO(List<DeviceEntity> deviceEntityList);
    List<DeviceEntity> deviceDTOToDeviceEntity(List<DeviceDTO> deviceDTOList);
}
