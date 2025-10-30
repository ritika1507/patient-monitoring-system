package com.healthcare.simulator.controller;

import com.healthcare.simulator.model.AnomalyType;
import com.healthcare.simulator.service.VitalsSimulatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * REST API for controlling the device simulator
 */
@RestController
@RequestMapping("/simulator")
@RequiredArgsConstructor
@Slf4j
public class SimulatorController {

    private final VitalsSimulatorService simulatorService;

    /**
     * Start monitoring a patient
     * POST /simulator/start/{patientId}
     */
    @PostMapping("/start/{patientId}")
    public ResponseEntity<Map> startMonitoring(@PathVariable String patientId) {
        log.info("Received request to start monitoring patient: {}", patientId);
        
        boolean started = simulatorService.startMonitoring(patientId);
        
        Map<String, String> response = new HashMap<>();
        response.put("patientId", patientId);
        response.put("status", started ? "started" : "already_running");
        response.put("message", started ? 
            "Monitoring started successfully" : 
            "Monitoring already active for this patient");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Stop monitoring a patient
     * POST /simulator/stop/{patientId}
     */
    @PostMapping("/stop/{patientId}")
    public ResponseEntity<Map> stopMonitoring(@PathVariable String patientId) {
        log.info("Received request to stop monitoring patient: {}", patientId);
        
        boolean stopped = simulatorService.stopMonitoring(patientId);
        
        Map<String, String> response = new HashMap<>();
        response.put("patientId", patientId);
        response.put("status", stopped ? "stopped" : "not_running");
        response.put("message", stopped ? 
            "Monitoring stopped successfully" : 
            "No active monitoring found for this patient");
        
        return stopped ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    /**
     * Get list of active simulations
     * GET /simulator/active
     */
    @GetMapping("/active")
    public ResponseEntity<Map> getActiveSimulations() {
        Set<String> activePatients = simulatorService.getActivePatients();
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", activePatients.size());
        response.put("patients", activePatients);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Inject an anomaly for testing
     * POST /simulator/inject-anomaly/{patientId}?type=HIGH_HEART_RATE
     */
    @PostMapping("/inject-anomaly/{patientId}")
    public ResponseEntity<Map> injectAnomaly(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "HIGH_HEART_RATE") AnomalyType type) {
        
        log.info("Injecting {} anomaly for patient {}", type, patientId);
        
        try {
            simulatorService.injectAnomaly(patientId, type);
            
            Map<String, Object> response = new HashMap<>();
            response.put("patientId", patientId);
            response.put("anomalyType", type);
            response.put("message", "Anomaly injected successfully (will auto-reset in 30 seconds)");
            
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Health check endpoint
     * GET /simulator/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "device-simulator");
        return ResponseEntity.ok(response);
    }
}