package org.example.deviceservice.mapper;

import org.example.deviceservice.dto.UserDTO;
import org.example.deviceservice.entity.UserEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, DeviceMapper.class})
public interface UserMapper {
    UserDTO userEntityToUserDTO(UserEntity userEntity);
    List<UserDTO> userEntityToUserDTO(List<UserEntity> userEntities);
    UserEntity userDTOToUserEntity(UserDTO userDTO);
    List<UserEntity> userDTOToUserEntity(List<UserDTO> userDTOs);
}
