package org.example.deviceservice.service;


import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.example.deviceservice.config.RabbitMQConfig;
import org.example.deviceservice.dto.DeviceDTO;
import org.example.deviceservice.dto.UserDTO;
import org.example.deviceservice.entity.DeviceEntity;
import org.example.deviceservice.entity.UserEntity;
import org.example.deviceservice.mapper.DeviceMapper;
import org.example.deviceservice.mapper.UserMapper;
import org.example.deviceservice.repository.DeviceRepository;
import org.example.deviceservice.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final DeviceMapper deviceMapper;
    private final UserMapper userMapper;
    private final RabbitTemplate rabbitTemplate;

    public List<DeviceDTO> findAll() {return deviceMapper.deviceEntityToDeviceDTO(deviceRepository.findAll());}

    public Long insertDevice(DeviceDTO deviceDTO) {
        DeviceEntity device = deviceMapper.deviceDTOToDeviceEntity(deviceDTO);
        UserEntity user = device.getUser();

        if (!userRepository.existsById(user.getId())) {
            throw new RuntimeException("User not found!");
        }

        deviceRepository.save(device);

        DeviceDTO dto = deviceMapper.deviceEntityToDeviceDTO(device);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "device.insert", dto);
        return device.getId();
    }

    public Long insertUser(UserDTO userDTO) {
        if(userRepository.existsById(userDTO.getId())) {
            throw new RuntimeException("User already exists!");
        }
        UserEntity userEntity = userMapper.userDTOToUserEntity(userDTO);

        return userRepository.save(userEntity).getId();
    }

    public void deleteUser(Long userId) {
        if(!userRepository.existsById(userId)){
            throw new RuntimeException("User not found!");
        }
        userRepository.deleteById(userId);
    }

    public void deleteDevice(Long deviceId) {
        if(!deviceRepository.existsById(deviceId)){
            throw new RuntimeException("Device not found!");
        }

        deviceRepository.deleteById(deviceId);

        DeviceDTO dto = DeviceDTO.builder()
                .id(deviceId)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "device.delete", dto);
    }

    @Transactional
    public Long updateDevice(DeviceDTO deviceDTO, Long deviceId) {
        DeviceEntity deviceEntity = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found!"));

        deviceMapper.updateDeviceEntityFromDeviceDTO(deviceDTO, deviceEntity);

        return deviceId;
    }

    public List<DeviceDTO> findDevicesByUserId(Long userId) {
        return deviceMapper.deviceEntityToDeviceDTO(deviceRepository.findByUserId(userId));
    }
}
