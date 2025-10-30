package com.healthcare.simulator.model;

/**
 * Types of anomalies that can be injected for testing alerts
 */
public enum AnomalyType {
    HIGH_HEART_RATE,      // Heart rate > 120 bpm
    LOW_HEART_RATE,       // Heart rate < 50 bpm
    LOW_OXYGEN,           // Oxygen < 90%
    HIGH_TEMPERATURE,     // Temperature > 38.5°C
    HIGH_BLOOD_PRESSURE,  // Systolic > 140 or Diastolic > 90
    HEART_ATTACK,         // Multiple critical vitals
    NORMAL                // Reset to normal vitals
}