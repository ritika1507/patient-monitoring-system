package com.healthcare.ingestion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity for patients table in PostgreSQL
 */
@Entity
@Table(name = "patients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    
    @Id
    private String id;
    
    private String name;
    private Integer age;
    
    @Column(name = "room_number")
    private String roomNumber;
    
    @Column(name = "assigned_doctor")
    private String assignedDoctor;
    
    private String status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}