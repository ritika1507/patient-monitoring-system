package com.healthcare.ingestion.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Vitals data model - matches the format from Device Simulator
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitalsData {
    private String patientId;
    private Integer heartRate;
    private String bloodPressure;
    private Integer oxygenLevel;
    private Double temperature;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;
    
    private String deviceId;
    private Boolean isAnomaly;
}
