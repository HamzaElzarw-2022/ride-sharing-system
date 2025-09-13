package com.rss.core.trip.infrastructure;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String DRIVER_REQUEST_QUEUE_NAME = "driver.request.queue";
    public static final String TRIP_ENDED_QUEUE_NAME = "trip.ended.queue";
    public static final String EXCHANGE_NAME = "trip.events.exchange";
    public static final String DRIVER_REQUEST_ROUTING_KEY = "driver.request";
    public static final String TRIP_ENDED_ROUTING_KEY = "trip.ended";

    @Bean
    public Queue driverRequestQueue() {
        return QueueBuilder.durable(DRIVER_REQUEST_QUEUE_NAME).build();
    }

    @Bean
    public Queue tripEndedQueue() {
        return QueueBuilder.durable(TRIP_ENDED_QUEUE_NAME).build();
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding driverRequestBinding(Queue driverRequestQueue, DirectExchange exchange) {
        return BindingBuilder
                .bind(driverRequestQueue)
                .to(exchange)
                .with(DRIVER_REQUEST_ROUTING_KEY);
    }

    @Bean
    public Binding tripEndedBinding(Queue tripEndedQueue, DirectExchange exchange) {
        return BindingBuilder
                .bind(tripEndedQueue)
                .to(exchange)
                .with(TRIP_ENDED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
