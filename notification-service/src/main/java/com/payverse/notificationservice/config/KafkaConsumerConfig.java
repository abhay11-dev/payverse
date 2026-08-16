package com.payverse.notificationservice.config;

import com.payverse.notificationservice.event.PaymentEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, PaymentEvent> consumerFactory() {

        JsonDeserializer<PaymentEvent> deserializer =
                new JsonDeserializer<>(PaymentEvent.class);

        /*
         * IMPORTANT:
         *
         * Payment-service writes its Java class name into
         * Kafka JSON type headers:
         *
         * com.payverse.paymentservice.event.PaymentEvent
         *
         * Notification-service has its own PaymentEvent class:
         *
         * com.payverse.notificationservice.event.PaymentEvent
         *
         * Therefore we ignore the producer's Java type header
         * and always deserialize into our local PaymentEvent.
         */
        deserializer.setUseTypeHeaders(false);

        deserializer.addTrustedPackages(
                "com.payverse.notificationservice.event"
        );

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092"
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "notification-service-group"
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentEvent>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, PaymentEvent>
                factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        return factory;
    }
}
