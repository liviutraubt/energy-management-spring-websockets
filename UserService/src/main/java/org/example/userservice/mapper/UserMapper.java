package org.example.userservice.mapper;

import org.example.userservice.dto.UserDTO;
import org.example.userservice.entity.UserEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface UserMapper {

    UserDTO userEntityToUserDTO(UserEntity user);
    List<UserDTO> userEntityToUserDTO(List<UserEntity> users);
    UserEntity userDTOToUserEntity(UserDTO userDTO);
    List<UserEntity> userDTOToUserEntity(List<UserDTO> userDTOs);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateUserEntityFromUserDTO(UserDTO userDTO, @MappingTarget UserEntity userEntity);
}
