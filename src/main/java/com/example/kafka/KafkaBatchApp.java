package com.example.kafka;

import com.example.avro.UserEvent;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class KafkaBatchApp {
    private static final Logger logger = LoggerFactory.getLogger(KafkaBatchApp.class);
    private static final String TOPIC = "user-events";

    public static void main(String[] args) {
        SpringApplication.run(KafkaBatchApp.class, args);
    }

    @Bean
    public CommandLineRunner sendBatchAvroEvents() {
        return args -> {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");
            props.put("schema.registry.url", "http://localhost:8081");
            // Add batching configs
//            props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
//            props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
//            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

            try (Producer<String, UserEvent> producer = new KafkaProducer<>(props)) {
                // Send more messages to ensure batching
                for (int i = 1; i <= 20; i++) {
                    UserEvent event = UserEvent.newBuilder()
                            .setUserId("user-" + i)
                            .setAction("click")
                            .setTimestamp(System.currentTimeMillis())
                            .build();

                    ProducerRecord<String, UserEvent> record =
                            new ProducerRecord<>(TOPIC, event.getUserId(), event);

                    try {
                        RecordMetadata metadata = producer.send(record).get();
                        logger.info("Sent message {} to topic={}, partition={}, offset={}",
                                i, metadata.topic(), metadata.partition(), metadata.offset());
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error("Error sending message {}: {}", i, e.getMessage(), e);
                    }
                }

                // Flush to ensure all messages are sent
                producer.flush();
                logger.info("Finished sending all messages");
            }
        };
    }
}
