package com.example.kafka;

import com.example.avro.UserEvent;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component("userEvents")
public class UserEventConsumer implements Consumer<Message<List<UserEvent>>> {

    @Override
    public void accept(Message<List<UserEvent>> message) {
        List<UserEvent> events = message.getPayload();
        for (UserEvent event : events) {
            System.out.printf("Received: userId=%s, action=%s, timestamp=%d%n",
                    event.getUserId(), event.getAction(), event.getTimestamp());
        }
    }

}