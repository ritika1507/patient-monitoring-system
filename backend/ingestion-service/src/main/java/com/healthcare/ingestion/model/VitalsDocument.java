package com.healthcare.ingestion.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TimeSeries;

import java.time.Instant;

/**
 * MongoDB document for storing vitals in time-series collection
 */
@Document(collection = "vitals")
@TimeSeries(timeField = "timestamp", metaField = "patientId")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitalsDocument {
    
    @Id
    private String id;
    
    @Indexed
    private String patientId;
    
    private Integer heartRate;
    private String bloodPressure;
    private Integer oxygenLevel;
    private Double temperature;
    
    @Indexed
    private Instant timestamp;
    
    private String deviceId;
    private Boolean isAnomaly;
    
    // Enriched data
    private Instant ingestedAt;
    private String status;  // VALID, INVALID, ANOMALY
}
