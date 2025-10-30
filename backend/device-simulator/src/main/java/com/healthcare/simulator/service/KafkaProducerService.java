package com.healthcare.simulator.service;

import com.healthcare.simulator.model.VitalsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for sending vitals data to Kafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate kafkaTemplate;

    @Value("${simulator.kafka.topic}")
    private String topic;

    /**
     * Sends vitals data to Kafka topic asynchronously
     * 
     * @param vitals The vital signs data to send
     */
    public void sendVitals(VitalsData vitals) {
        try {
            // Send message with patientId as key (ensures all messages for same patient go to same partition)
            CompletableFuture<SendResult> future = 
                kafkaTemplate.send(topic, vitals.getPatientId(), vitals);

            // Add callback for success/failure
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Sent vitals for patient {} to partition {} with offset {}",
                            vitals.getPatientId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send vitals for patient {}: {}",
                            vitals.getPatientId(), ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Error sending vitals to Kafka: {}", e.getMessage(), e);
        }
    }
}
