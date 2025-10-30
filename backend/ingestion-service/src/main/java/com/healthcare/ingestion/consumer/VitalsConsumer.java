package com.healthcare.ingestion.consumer;

import com.healthcare.ingestion.model.VitalsData;
import com.healthcare.ingestion.service.VitalsIngestionService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for patient vitals
 * Listens to patient-vitals topic
 */
@Component
@Slf4j
public class VitalsConsumer {

    private final VitalsIngestionService ingestionService;
    private final Counter vitalsReceivedCounter;

    public VitalsConsumer(VitalsIngestionService ingestionService, MeterRegistry meterRegistry) {
        this.ingestionService = ingestionService;
        this.vitalsReceivedCounter = Counter.builder("vitals.received")
                .description("Number of vitals messages received")
                .register(meterRegistry);
    }

    /**
     * Consumes vitals messages from Kafka
     */
    @KafkaListener(
            topics = "${app.kafka.input-topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeVitals(
            @Payload VitalsData vitals,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("Received vitals for patient {} from partition {} offset {}", 
                vitals.getPatientId(), partition, offset);
        
        vitalsReceivedCounter.increment();
        ingestionService.processVitals(vitals);
    }
}