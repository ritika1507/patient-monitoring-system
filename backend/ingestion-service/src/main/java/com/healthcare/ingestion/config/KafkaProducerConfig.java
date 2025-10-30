package com.healthcare.ingestion.config;

import com.healthcare.ingestion.model.VitalsData;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Producer Configuration
 * Configures how we produce messages to Kafka topics
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Producer Factory - creates Kafka producers
     */
    @Bean
    public ProducerFactory<String, VitalsData> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        
        // Kafka broker address
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Serializers (convert objects to bytes)
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Wait for all replicas to acknowledge (durability)
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        
        // Retry failed sends
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        
        // Batch messages for efficiency
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        
        // Compress messages
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Kafka Template - high-level API for sending messages
     */
    @Bean
    public KafkaTemplate<String, VitalsData> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}