package com.rss.backend.trip.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RabbitProducerTest {

    @Autowired
    private RabbitProducer rabbitProducer;

    @Test
    void shouldPublishDriverRequestedEvent_withoutAssertions() {
        rabbitProducer.NotifyDriverRequest(123L, 456L);
    }
}
