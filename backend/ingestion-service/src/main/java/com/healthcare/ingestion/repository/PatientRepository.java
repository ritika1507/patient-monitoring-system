package com.healthcare.ingestion.repository;

import com.healthcare.ingestion.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for patient data in PostgreSQL
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {
}