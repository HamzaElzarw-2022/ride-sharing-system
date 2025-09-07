package com.rss.core.trip.infrastructure;

import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void receiveMessage(Map<String, Object> event) {
        System.out.println("Received: " + event);
    }
}
