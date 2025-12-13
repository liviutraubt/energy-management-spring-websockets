package org.example.monitorigservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.monitorigservice.dto.DeviceDTO;
import org.example.monitorigservice.dto.ExceededConsumptionDTO; // Importă DTO-ul nou
import org.example.monitorigservice.dto.MonitoringDTO;
import org.example.monitorigservice.entity.DeviceEntity;
import org.example.monitorigservice.entity.MonitoringEntity;
import org.example.monitorigservice.mapper.MonitoringMapper;
import org.example.monitorigservice.rabbit.RabbitConfig;
import org.example.monitorigservice.repository.DeviceRepository;
import org.example.monitorigservice.repository.MonitoringRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate; // Importă RabbitTemplate
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

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.DEVICE_MEASUREMENTS_QUEUE)
    public void handleMessage(String messageJson) {
        try {
            MonitoringDTO dto = objectMapper.readValue(messageJson, MonitoringDTO.class);
            MonitoringEntity monitoringEntity = monitoringMapper.monitoringDTOToMonitoringEntity(dto);

            DeviceEntity device = deviceRepository.findById(monitoringEntity.getDevice().getId())
                    .orElseThrow(() -> new RuntimeException("Device does not exist"));

            monitoringEntity.setDevice(device);

            LocalDateTime time = monitoringEntity.getTimestamp();
            time = time.withMinute(0).withSecond(0).withNano(0);
            monitoringEntity.setTimestamp(time);

            Double currentTotalConsumption;

            MonitoringEntity existing = monitoringRepository.findByDeviceIdAndTimestamp(monitoringEntity.getDevice().getId(), time);

            if (existing == null) {
                monitoringRepository.save(monitoringEntity);
                currentTotalConsumption = monitoringEntity.getConsumption();
            } else {
                Double aux = monitoringEntity.getConsumption();
                aux += existing.getConsumption();
                aux = BigDecimal.valueOf(aux).setScale(2, RoundingMode.HALF_UP).doubleValue();

                existing.setConsumption(aux);
                monitoringRepository.save(existing);

                currentTotalConsumption = aux;
            }

            checkAndSendNotification(device, time, currentTotalConsumption);

        } catch (Exception e) {
            System.err.println("[MONITORING] Eroare la procesarea mesajului: " + messageJson);
            e.printStackTrace();
        }
    }

    private void checkAndSendNotification(DeviceEntity device, LocalDateTime time, Double currentConsumption) {
        if (device.getMaximumConsumption() != null && currentConsumption > device.getMaximumConsumption()) {
            try {
                ExceededConsumptionDTO alertDTO = ExceededConsumptionDTO.builder()
                        .userId(device.getUserId())
                        .deviceId(device.getId())
                        .timestamp(time)
                        .actualConsumption(currentConsumption)
                        .limit(device.getMaximumConsumption())
                        .build();

                String alertJson = objectMapper.writeValueAsString(alertDTO);


                rabbitTemplate.convertAndSend(
                        RabbitConfig.EXCHANGE_NAME,
                        "device.exceeded",
                        alertJson
                );

                System.out.println("[ALERT] Mesaj trimis pentru device " + device.getId() + " - Consum depășit!");

            } catch (Exception e) {
                System.err.println("[ALERT] Nu s-a putut trimite alerta: " + e.getMessage());
            }
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
            } else if  (routingKey.endsWith("update")) {
                try {
                    monitoringService.updateDevice(deviceDTO);
                    System.out.println("[SYNC] Device updated: " + deviceDTO.getId());
                }catch (Exception e) {
                    System.err.println("[SYNC] Update failed: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[SYNC] Eroare la procesarea mesajului de sincronizare: " + messageJson);
        }
    }
}