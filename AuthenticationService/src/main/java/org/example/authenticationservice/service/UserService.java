package org.example.authenticationservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.config.RabbitMQConfig;
import org.example.authenticationservice.dto.LoginRequest;
import org.example.authenticationservice.dto.RegisterRequest;
import org.example.authenticationservice.dto.UserDTO;
import org.example.authenticationservice.dto.UserServiceDTO;
import org.example.authenticationservice.entity.Roles;
import org.example.authenticationservice.entity.UserEntity;
import org.example.authenticationservice.mapper.UserMapper;
import org.example.authenticationservice.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;
    private final RabbitTemplate rabbitTemplate;

    public Long registerUser(RegisterRequest registerRequest) {
        if(userRepository.existsByUsername(registerRequest.username())){
            throw new RuntimeException("Username already exists");
        }

        UserEntity user = UserEntity.builder()
                .username(registerRequest.username())
                .password(encoder.encode(registerRequest.password()))
                .role(Roles.USER)
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.insert", user);

        return userRepository.save(user).getId();
    }

    public UserDTO login(LoginRequest loginRequest) {
        UserEntity user = userRepository.findByUsername(loginRequest.username()).orElseThrow(() -> new RuntimeException("Username not found"));
        if(encoder.matches(loginRequest.password(), user.getPassword()) && user != null) {
            userRepository.save(user);
            return userMapper.userEntityToUserDTO(user);
        }

        return null;
    }

    public List<UserDTO> getUsers() {return userMapper.userEntityToUserDTO(userRepository.findAll());}

    public Long deleteUser(Long id){
        if(userRepository.existsById(id)){
            userRepository.deleteById(id);

            UserServiceDTO userServiceDTO = UserServiceDTO.builder().id(id).build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.delete", userServiceDTO);

        }
        else {
            throw new RuntimeException("User not found");
        }
        return id;
    }

    public Long adminRegisterUser(RegisterRequest registerRequest) {
        if(userRepository.existsByUsername(registerRequest.username())){
            throw new RuntimeException("Username already exists");
        }

        var user = UserEntity.builder()
                .username(registerRequest.username())
                .password(encoder.encode(registerRequest.password()))
                .role(registerRequest.role())
                .build();

        UserEntity savedUser = userRepository.save(user);

        UserServiceDTO userMessage = UserServiceDTO.builder()
                .id(savedUser.getId())
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .email(registerRequest.email())
                .telephone(registerRequest.telephone())
                .address(registerRequest.address())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.insert", userMessage);

        return savedUser.getId();
    }
}
