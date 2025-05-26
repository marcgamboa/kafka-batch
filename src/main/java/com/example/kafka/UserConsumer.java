package com.example.kafka;

import com.example.avro.UserEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.util.List;
import java.util.function.Consumer;

//@Component
public class UserConsumer {
    private static final Logger logger = LoggerFactory.getLogger(UserConsumer.class);

//    @Bean
    public Consumer<Message<List<UserEvent>>> userEvents(StreamBridge streamBridge) {
        return message -> {
            try {
                MessageHeaders headers = message.getHeaders();

                // Debug raw message before getting payload
                logger.debug("Received raw message: {}", message);
                logger.debug("Message class: {}", message.getClass().getName());
                logger.debug("All headers: {}", headers);

                // Log content-type and other important headers
                logger.debug("Content-Type: {}", headers.get("contentType"));
                logger.debug("Kafka topic: {}", headers.get(KafkaHeaders.RECEIVED_TOPIC));
                logger.debug("Kafka partition: {}", headers.get(KafkaHeaders.RECEIVED_PARTITION_ID));
                logger.debug("Kafka timestamp: {}", headers.get(KafkaHeaders.RECEIVED_TIMESTAMP));
                logger.debug("Raw payload before cast: {}", message.getPayload());
                logger.debug("Payload class: {}", message.getPayload().getClass().getName());

                List<UserEvent> events = message.getPayload();
                logger.info("Received batch of {} messages", events != null ? events.size() : 0);

                if (events == null || events.isEmpty()) {
                    logger.warn("Received empty or null batch of messages");
                    return;
                }

                for (UserEvent event : events) {
                    try {
                        if (event == null) {
                            logger.warn("Null event in batch");
                            continue;
                        }

                        logger.info("Processing event - userId: {}, action: {}, timestamp: {}",
                                event.getUserId(), event.getAction(), event.getTimestamp());
                        logger.debug("Raw event data: {}", event);

                    } catch (Exception e) {
                        logger.error("Error processing individual event: {}", e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing message batch: {}", e.getMessage(), e);
                logger.error("Full error details:", e);
            }
        };
    }
}
