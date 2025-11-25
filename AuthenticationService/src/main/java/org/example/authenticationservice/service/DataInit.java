package org.example.authenticationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.config.RabbitMQConfig;
import org.example.authenticationservice.dto.UserServiceDTO;
import org.example.authenticationservice.entity.Roles;
import org.example.authenticationservice.entity.UserEntity;
import org.example.authenticationservice.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void run(String... args){
        if(!userRepository.existsByUsername("admin")){
            UserEntity admin = UserEntity.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .role(Roles.ADMIN)
                    .build();
            userRepository.save(admin);

            UserServiceDTO userServiceDTO = UserServiceDTO.builder()
                    .id(admin.getId())
                    .firstName("admin")
                    .lastName("admin")
                    .address("Strada Baritiu")
                    .email("admin@admin.com")
                    .telephone("0712345678")
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.insert", userServiceDTO);
        }
    }
}
