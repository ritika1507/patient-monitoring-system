package com.healthcare.ingestion.config;

import com.healthcare.ingestion.model.VitalsData;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer Configuration
 * Configures how we consume messages from Kafka topics
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Consumer Factory - creates Kafka consumers
     */
    @Bean
    public ConsumerFactory<String, VitalsData> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        
        // Kafka broker address
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Consumer group ID (allows multiple consumers to share load)
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        
        // Deserializers (convert bytes back to objects)
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Auto offset reset - start from earliest if no offset exists
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // Enable auto commit (automatically save consumer position)
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        
        // JSON deserializer config (trust all packages for deserialization)
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, VitalsData.class.getName());
        
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new JsonDeserializer<>(VitalsData.class, false)
        );
    }

    /**
     * Kafka Listener Container Factory
     * Creates containers that wrap consumers and handle message processing
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VitalsData> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, VitalsData> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // Number of concurrent consumers (parallelism)
        factory.setConcurrency(3);
        
        // Batch listener disabled (process one message at a time)
        factory.setBatchListener(false);
        
        return factory;
    }
}