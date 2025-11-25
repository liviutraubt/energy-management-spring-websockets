package org.example.userservice.service;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.UserDTO;
import org.example.userservice.entity.UserEntity;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.validator.UserValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserValidator userValidator;

    public List<UserDTO> findAllUsers() {return userMapper.userEntityToUserDTO(userRepository.findAll());}

    public Long insertUser(UserDTO userDTO) {
        UserEntity userEntity = userMapper.userDTOToUserEntity(userDTO);
        String error = userValidator.validate(userEntity);

        if(!error.isEmpty()){
            throw  new ValidationException(error);
        }

        return userRepository.save(userEntity).getId();
    }

    public void deleteUser(Long userId) {
        if(!userRepository.existsById(userId)){
            throw new RuntimeException("User not found!");
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public Long updateUser(Long userId, UserDTO userDTO) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        userMapper.updateUserEntityFromUserDTO(userDTO, userEntity);
        String error = userValidator.validate(userEntity);

        if(!error.isEmpty()){
            throw  new ValidationException(error);
        }

        return userId;
    }
}
