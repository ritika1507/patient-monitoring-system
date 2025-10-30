package com.healthcare.ingestion.repository;

import com.healthcare.ingestion.model.VitalsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for vitals data in MongoDB
 */
@Repository
public interface VitalsRepository extends MongoRepository<VitalsDocument, String> {
    
    /**
     * Find vitals for a patient within time range
     */
    List<VitalsDocument> findByPatientIdAndTimestampBetweenOrderByTimestampDesc(
        String patientId, Instant start, Instant end);
    
    /**
     * Find latest N vitals for a patient
     */
    List<VitalsDocument> findTop10ByPatientIdOrderByTimestampDesc(String patientId);
    
    /**
     * Find anomalies for a patient
     */
    List<VitalsDocument> findByPatientIdAndIsAnomalyTrueOrderByTimestampDesc(String patientId);
}