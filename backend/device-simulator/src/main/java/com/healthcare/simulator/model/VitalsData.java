package com.healthcare.simulator.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Represents patient vital signs data
 * This is the core data structure sent to Kafka
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitalsData {

    private String patientId;
    
    // Heart rate in beats per minute (bpm)
    // Normal: 60-100, Warning: 100-120, Critical: >120 or <50
    private Integer heartRate;
    
    // Blood pressure in format "systolic/diastolic" (e.g., "120/80")
    // Normal: <120/80, Warning: 120-139/80-89, Critical: >140/90
    private String bloodPressure;
    
    // Oxygen saturation level (SpO2) as percentage
    // Normal: 95-100%, Warning: 90-94%, Critical: <90%
    private Integer oxygenLevel;
    
    // Body temperature in Celsius
    // Normal: 36.5-37.5, Warning: 37.5-38.5, Critical: >38.5
    private Double temperature;
    
    // Timestamp when vitals were recorded
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;
    
    // Device ID (optional, for tracking which device sent the data)
    private String deviceId;
    
    // Is this an anomaly (for testing/demo purposes)
    private Boolean isAnomaly;
}