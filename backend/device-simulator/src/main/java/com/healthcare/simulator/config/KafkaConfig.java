package com.healthcare.simulator.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka configuration
 * Creates topics automatically on startup
 */
@Configuration
public class KafkaConfig {

    @Value("${simulator.kafka.topic}")
    private String topic;

    /**
     * Creates the patient-vitals topic if it doesn't exist
     * 3 partitions for parallel processing
     * Replication factor 1 (single broker)
     */
    @Bean
    public NewTopic patientVitalsTopic() {
        return TopicBuilder.name(topic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}