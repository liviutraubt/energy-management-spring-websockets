package org.example.deviceservice.mapper;

import org.example.deviceservice.dto.DeviceDTO;
import org.example.deviceservice.entity.DeviceEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, DeviceMapper.class})
public interface DeviceMapper {
    DeviceDTO deviceEntityToDeviceDTO(DeviceEntity deviceEntity);
    DeviceEntity deviceDTOToDeviceEntity(DeviceDTO deviceDTO);
    List<DeviceDTO> deviceEntityToDeviceDTO(List<DeviceEntity> deviceEntities);
    List<DeviceEntity> deviceDTOToDeviceEntity(List<DeviceDTO> deviceDTOs);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateDeviceEntityFromDeviceDTO(DeviceDTO deviceDTO, @MappingTarget DeviceEntity deviceEntity);
}
