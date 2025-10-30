package com.healthcare.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Device Simulator Service
 * Simulates IoT medical devices sending patient vitals to Kafka
 */
@SpringBootApplication
public class DeviceSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceSimulatorApplication.class, args);
    }
}