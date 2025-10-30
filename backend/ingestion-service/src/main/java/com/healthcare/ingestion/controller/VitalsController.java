package com.healthcare.ingestion.controller;

import com.healthcare.ingestion.model.VitalsData;
import com.healthcare.ingestion.model.VitalsDocument;
import com.healthcare.ingestion.repository.VitalsRepository;
import com.healthcare.ingestion.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for querying vitals data
 */
@RestController
@RequestMapping("/vitals")
@RequiredArgsConstructor
@Slf4j
public class VitalsController {

    private final VitalsRepository vitalsRepository;
    private final RedisService redisService;

    /**
     * Get latest vitals for a patient (from Redis cache)
     * GET /vitals/{patientId}/latest
     */
    @GetMapping("/{patientId}/latest")
    public ResponseEntity<VitalsData> getLatestVitals(@PathVariable String patientId) {
        log.info("Fetching latest vitals for patient {}", patientId);
        
        return redisService.getLatestVitals(patientId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    // Fallback to MongoDB if not in cache
                    List<VitalsDocument> vitals = vitalsRepository
                            .findTop10ByPatientIdOrderByTimestampDesc(patientId);

                    if (!vitals.isEmpty()) {
                        VitalsDocument latest = vitals.get(0);
                        VitalsData data = convertToDto(latest);
                        return ResponseEntity.ok(data);
                    }
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Get historical vitals for a patient
     * GET /vitals/{patientId}/history?from=2024-10-29T10:00:00Z&to=2024-10-29T11:00:00Z
     */
    @GetMapping("/{patientId}/history")
    public ResponseEntity<List<VitalsDocument>> getVitalsHistory(
            @PathVariable String patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        
        log.info("Fetching vitals history for patient {} from {} to {}", patientId, from, to);
        
        // Default to last 1 hour if not specified
        if (from == null) {
            from = Instant.now().minus(1, ChronoUnit.HOURS);
        }
        if (to == null) {
            to = Instant.now();
        }
        
    List<VitalsDocument> vitals = vitalsRepository
        .findByPatientIdAndTimestampBetweenOrderByTimestampDesc(patientId, from, to);

    return ResponseEntity.ok(vitals);
    }

    /**
     * Get anomalies for a patient
     * GET /vitals/{patientId}/anomalies
     */
    @GetMapping("/{patientId}/anomalies")
    public ResponseEntity<List<VitalsDocument>> getAnomalies(@PathVariable String patientId) {
        log.info("Fetching anomalies for patient {}", patientId);
        
    List<VitalsDocument> anomalies = vitalsRepository
        .findByPatientIdAndIsAnomalyTrueOrderByTimestampDesc(patientId);

    return ResponseEntity.ok(anomalies);
    }

    /**
     * Get statistics for a patient
     * GET /vitals/{patientId}/stats
     */
    @GetMapping("/{patientId}/stats")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable String patientId) {
        log.info("Fetching stats for patient {}", patientId);
        
        Instant from = Instant.now().minus(24, ChronoUnit.HOURS);
        Instant to = Instant.now();
        
    List<VitalsDocument> vitals = vitalsRepository
        .findByPatientIdAndTimestampBetweenOrderByTimestampDesc(patientId, from, to);
        
    Map<String, Object> stats = new HashMap<>();
        stats.put("patientId", patientId);
        stats.put("totalRecords", vitals.size());
        stats.put("timeRange", Map.of("from", from, "to", to));
        
        if (!vitals.isEmpty()) {
        double avgHeartRate = vitals.stream()
            .mapToInt(VitalsDocument::getHeartRate)
            .average()
            .orElse(0.0);
            
        double avgOxygen = vitals.stream()
            .mapToInt(VitalsDocument::getOxygenLevel)
            .average()
            .orElse(0.0);
            
        long anomalyCount = vitals.stream()
            .filter(v -> Boolean.TRUE.equals(v.getIsAnomaly()))
            .count();
            
            stats.put("averageHeartRate", avgHeartRate);
            stats.put("averageOxygenLevel", avgOxygen);
            stats.put("anomalyCount", anomalyCount);
        }
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map> health() {
        Map response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "ingestion-service");
        return ResponseEntity.ok(response);
    }

    // Helper method
    private VitalsData convertToDto(VitalsDocument doc) {
        return VitalsData.builder()
                .patientId(doc.getPatientId())
                .heartRate(doc.getHeartRate())
                .bloodPressure(doc.getBloodPressure())
                .oxygenLevel(doc.getOxygenLevel())
                .temperature(doc.getTemperature())
                .timestamp(doc.getTimestamp())
                .deviceId(doc.getDeviceId())
                .isAnomaly(doc.getIsAnomaly())
                .build();
    }
}