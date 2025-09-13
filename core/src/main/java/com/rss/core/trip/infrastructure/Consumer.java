package com.rss.core.trip.infrastructure;

import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {

    @RabbitListener(queues = RabbitConfig.DRIVER_REQUEST_QUEUE_NAME)
    public void receiveDriverRequestMessage(Map<String, Object> event) {
        System.out.println("Received: " + event);
    }

    @RabbitListener(queues = RabbitConfig.TRIP_ENDED_QUEUE_NAME)
    public void receiveTripEndedMessage(Map<String, Object> event) {
        System.out.println("Received trip ended: " + event);
    }
}
