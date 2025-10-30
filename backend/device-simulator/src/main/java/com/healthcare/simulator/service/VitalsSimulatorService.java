package com.healthcare.simulator.service;

import com.healthcare.simulator.model.AnomalyType;
import com.healthcare.simulator.model.VitalsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Core service that simulates IoT medical devices
 * Generates realistic vitals data and handles anomaly injection
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VitalsSimulatorService {

    private final KafkaProducerService kafkaProducerService;
    private final Random random = new Random();
    
    // Thread pool for running simulations
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    
    // Track active simulations: patientId -> ScheduledFuture
private final Map<String, ScheduledFuture<?>>  activeSimulations = new ConcurrentHashMap<>();
    
    // Track anomaly state for each patient
    private final Map<String, AnomalyType> patientAnomalies = new ConcurrentHashMap<>();

    @Value("${simulator.vitals.interval-min-ms}")
    private int intervalMinMs;

    @Value("${simulator.vitals.interval-max-ms}")
    private int intervalMaxMs;

    /**
     * Start monitoring a patient (simulate device sending vitals)
     */
    public boolean startMonitoring(String patientId) {
        if (activeSimulations.containsKey(patientId)) {
            log.warn("Monitoring already active for patient {}", patientId);
            return false;
        }

        log.info("Starting monitoring for patient {}", patientId);
        
        // Schedule periodic vitals generation
        ScheduledFuture future = scheduler.scheduleWithFixedDelay(
            () -> generateAndSendVitals(patientId),
            0,  // Initial delay
            getRandomInterval(),  // Random interval between 2-5 seconds
            TimeUnit.MILLISECONDS
        );
        
        activeSimulations.put(patientId, future);
        return true;
    }

    /**
     * Stop monitoring a patient
     */
    public boolean stopMonitoring(String patientId) {
        ScheduledFuture future = activeSimulations.remove(patientId);
        if (future != null) {
            future.cancel(false);
            patientAnomalies.remove(patientId);
            log.info("Stopped monitoring for patient {}", patientId);
            return true;
        }
        log.warn("No active monitoring found for patient {}", patientId);
        return false;
    }

    /**
     * Get list of patients currently being monitored
     */
    public Set<String> getActivePatients() {
        return activeSimulations.keySet();
    }

    /**
     * Inject an anomaly for testing alerts
     */
    public void injectAnomaly(String patientId, AnomalyType anomalyType) {
        if (!activeSimulations.containsKey(patientId)) {
            throw new IllegalStateException("Patient " + patientId + " is not being monitored");
        }
        
        patientAnomalies.put(patientId, anomalyType);
        log.info("Injected {} anomaly for patient {}", anomalyType, patientId);
        
        // Auto-reset to normal after 30 seconds
        scheduler.schedule(() -> {
            if (patientAnomalies.get(patientId) == anomalyType) {
                patientAnomalies.put(patientId, AnomalyType.NORMAL);
                log.info("Auto-reset patient {} to normal vitals", patientId);
            }
        }, 30, TimeUnit.SECONDS);
    }

    /**
     * Generate and send vitals data to Kafka
     */
    private void generateAndSendVitals(String patientId) {
        try {
            AnomalyType anomaly = patientAnomalies.getOrDefault(patientId, AnomalyType.NORMAL);
            VitalsData vitals = generateVitals(patientId, anomaly);
            kafkaProducerService.sendVitals(vitals);
        } catch (Exception e) {
            log.error("Error generating vitals for patient {}: {}", patientId, e.getMessage());
        }
    }

    /**
     * Generate realistic vitals data based on anomaly type
     */
    private VitalsData generateVitals(String patientId, AnomalyType anomaly) {
        VitalsData.VitalsDataBuilder builder = VitalsData.builder()
            .patientId(patientId)
            .timestamp(Instant.now())
            .deviceId("DEVICE-" + patientId)
            .isAnomaly(!anomaly.equals(AnomalyType.NORMAL));

        switch (anomaly) {
            case HIGH_HEART_RATE:
                builder.heartRate(randomBetween(125, 150))
                       .bloodPressure(generateNormalBP())
                       .oxygenLevel(randomBetween(95, 100))
                       .temperature(randomDouble(36.5, 37.5));
                break;

            case LOW_HEART_RATE:
                builder.heartRate(randomBetween(35, 48))
                       .bloodPressure(generateNormalBP())
                       .oxygenLevel(randomBetween(95, 100))
                       .temperature(randomDouble(36.5, 37.5));
                break;

            case LOW_OXYGEN:
                builder.heartRate(randomBetween(60, 100))
                       .bloodPressure(generateNormalBP())
                       .oxygenLevel(randomBetween(75, 88))
                       .temperature(randomDouble(36.5, 37.5));
                break;

            case HIGH_TEMPERATURE:
                builder.heartRate(randomBetween(85, 110))
                       .bloodPressure(generateNormalBP())
                       .oxygenLevel(randomBetween(95, 100))
                       .temperature(randomDouble(38.6, 40.0));
                break;

            case HIGH_BLOOD_PRESSURE:
                builder.heartRate(randomBetween(70, 90))
                       .bloodPressure(generateHighBP())
                       .oxygenLevel(randomBetween(95, 100))
                       .temperature(randomDouble(36.5, 37.5));
                break;

            case HEART_ATTACK:
                // Multiple critical vitals at once
                builder.heartRate(randomBetween(140, 180))
                       .bloodPressure(generateHighBP())
                       .oxygenLevel(randomBetween(80, 88))
                       .temperature(randomDouble(37.0, 38.0));
                break;

            default:  // NORMAL
                builder.heartRate(randomBetween(60, 100))
                       .bloodPressure(generateNormalBP())
                       .oxygenLevel(randomBetween(95, 100))
                       .temperature(randomDouble(36.5, 37.5));
        }

        return builder.build();
    }

    // Helper methods for generating realistic data

    private String generateNormalBP() {
        int systolic = randomBetween(110, 130);
        int diastolic = randomBetween(70, 85);
        return systolic + "/" + diastolic;
    }

    private String generateHighBP() {
        int systolic = randomBetween(145, 170);
        int diastolic = randomBetween(92, 110);
        return systolic + "/" + diastolic;
    }

    private int randomBetween(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    private double randomDouble(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    private long getRandomInterval() {
        return randomBetween(intervalMinMs, intervalMaxMs);
    }

    /**
     * Shutdown hook to clean up resources
     */
    public void shutdown() {
        log.info("Shutting down simulator service...");
        activeSimulations.values().forEach(future -> future.cancel(false));
        scheduler.shutdown();
    }
}