package org.example.monitorigservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.monitorigservice.dto.DeviceDTO;
import org.example.monitorigservice.dto.MonitoringDTO;
import org.example.monitorigservice.entity.MonitoringEntity;
import org.example.monitorigservice.mapper.MonitoringMapper;
import org.example.monitorigservice.rabbit.RabbitConfig;
import org.example.monitorigservice.repository.DeviceRepository;
import org.example.monitorigservice.repository.MonitoringRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MonitoringListener {

    private final ObjectMapper objectMapper;
    private final DeviceRepository deviceRepository;
    private final MonitoringRepository monitoringRepository;
    private final MonitoringMapper monitoringMapper;

    private final MonitoringService monitoringService;

    @RabbitListener(queues = RabbitConfig.DEVICE_MEASUREMENTS_QUEUE)
    public void handleMessage(String messageJson) {
        try {
            MonitoringDTO dto = objectMapper.readValue(messageJson, MonitoringDTO.class);
            MonitoringEntity monitoringEntity = monitoringMapper.monitoringDTOToMonitoringEntity(dto);

            if(!deviceRepository.existsById(monitoringEntity.getDevice().getId())) {
                throw new RuntimeException("Device does not exist");
            }

            LocalDateTime time = monitoringEntity.getTimestamp();
            time = time.withMinute(0).withSecond(0).withNano(0);
            monitoringEntity.setTimestamp(time);

            if(monitoringRepository.findByDeviceIdAndTimestamp(monitoringEntity.getDevice().getId(), time) == null) {
                monitoringRepository.save(monitoringEntity);
            }
            else{
                MonitoringEntity existing =  monitoringRepository.findByDeviceIdAndTimestamp(monitoringEntity.getDevice().getId(), time);

                Double aux = monitoringEntity.getConsumption();
                aux += existing.getConsumption();
                aux = BigDecimal.valueOf(aux).setScale(2, RoundingMode.HALF_UP).doubleValue();
                existing.setConsumption(aux);

                monitoringRepository.save(existing);
            }
        } catch (Exception e) {
            System.err.println("[MONITORING] Eroare la parsarea mesajului JSON: " + messageJson);
            e.printStackTrace();
        }
    }

    @RabbitListener(queues = RabbitConfig.DEVICE_SYNC_QUEUE)
    public void handleDeviceSync(String messageJson, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        try {
            DeviceDTO deviceDTO = objectMapper.readValue(messageJson, DeviceDTO.class);

            if (routingKey.endsWith("insert")) {
                try {
                    monitoringService.insertDevice(deviceDTO);
                    System.out.println("[SYNC] Device inserted: " + deviceDTO.getId());
                } catch (Exception e) {
                    System.err.println("[SYNC] Insert failed (might already exist): " + e.getMessage());
                }
            } else if (routingKey.endsWith("delete")) {
                try {
                    monitoringService.deleteDevice(deviceDTO.getId());
                    System.out.println("[SYNC] Device deleted: " + deviceDTO.getId());
                } catch (Exception e) {
                    System.err.println("[SYNC] Delete failed (might not exist): " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[SYNC] Eroare la procesarea mesajului de sincronizare: " + messageJson);
        }
    }
}
