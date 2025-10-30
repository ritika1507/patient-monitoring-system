package com.healthcare.ingestion.service;

import com.healthcare.ingestion.model.Patient;
import com.healthcare.ingestion.model.VitalsData;
import com.healthcare.ingestion.model.VitalsDocument;
import com.healthcare.ingestion.repository.PatientRepository;
import com.healthcare.ingestion.repository.VitalsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Core service for ingesting and processing vitals data
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VitalsIngestionService {

    private final VitalsRepository vitalsRepository;
    private final PatientRepository patientRepository;
    private final RedisService redisService;
    private final KafkaTemplate<String, VitalsData> kafkaTemplate;

    @Value("${app.kafka.output-topic}")
    private String outputTopic;

    /**
     * Process incoming vitals data
     * 1. Validate patient exists
     * 2. Store in MongoDB
     * 3. Cache in Redis
     * 4. Publish to next topic
     */
    public void processVitals(VitalsData vitals) {
        try {
            log.debug("Processing vitals for patient {}", vitals.getPatientId());

            // 1. Validate patient exists
            if (!validatePatient(vitals.getPatientId())) {
                log.warn("Invalid patient ID: {}. Skipping vitals.", vitals.getPatientId());
                return;
            }

            // 2. Store in MongoDB (time-series)
            VitalsDocument document = convertToDocument(vitals);
            vitalsRepository.save(document);
            log.debug("Stored vitals in MongoDB for patient {}", vitals.getPatientId());

            // 3. Cache latest vitals in Redis
            redisService.cacheLatestVitals(vitals);

            // 4. Publish to Redis Pub/Sub for real-time updates
            redisService.publishVitalsUpdate(vitals);

            // 5. Publish to Kafka for downstream processing (Alert Engine)
            kafkaTemplate.send(outputTopic, vitals.getPatientId(), vitals);
            log.debug("Published vitals to {} topic", outputTopic);

        } catch (Exception e) {
            log.error("Error processing vitals for patient {}: {}", 
                    vitals.getPatientId(), e.getMessage(), e);
        }
    }

    /**
     * Validate that patient exists in database
     */
    private boolean validatePatient(String patientId) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        return patient.isPresent() && "ACTIVE".equals(patient.get().getStatus());
    }

    /**
     * Convert VitalsData to MongoDB document
     */
    private VitalsDocument convertToDocument(VitalsData vitals) {
        return VitalsDocument.builder()
                .patientId(vitals.getPatientId())
                .heartRate(vitals.getHeartRate())
                .bloodPressure(vitals.getBloodPressure())
                .oxygenLevel(vitals.getOxygenLevel())
                .temperature(vitals.getTemperature())
                .timestamp(vitals.getTimestamp())
                .deviceId(vitals.getDeviceId())
                .isAnomaly(vitals.getIsAnomaly())
                .ingestedAt(Instant.now())
                .status("VALID")
                .build();
    }
}