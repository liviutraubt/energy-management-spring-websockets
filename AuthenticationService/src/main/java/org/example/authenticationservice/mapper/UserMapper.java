package org.example.authenticationservice.mapper;

import org.example.authenticationservice.dto.UserDTO;
import org.example.authenticationservice.entity.UserEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDTO userEntityToUserDTO(UserEntity user);
    List<UserDTO> userEntityToUserDTO(List<UserEntity> users);

    UserEntity userDTOToUserEntity(UserDTO userDTO);
    List<UserEntity> userDTOToUserEntity(List<UserDTO> userDTOs);
}
