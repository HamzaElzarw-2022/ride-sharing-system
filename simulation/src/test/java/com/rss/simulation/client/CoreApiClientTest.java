package com.rss.simulation.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rss.simulation.client.dto.Point;
import com.rss.simulation.client.dto.RouteRequest;
import com.rss.simulation.client.dto.SimRouteRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CoreApiClientTest {

    @Autowired
    private CoreApiClient coreApiClient;

    @Test
    void getRoute() throws JsonProcessingException {
        var res = coreApiClient.getRoute(new Point(10,25), new Point(300,450), null).block();

        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(res));
    }

    @Test
    void getSimRoute() throws JsonProcessingException {
        var request = new SimRouteRequest(50, 13L, new Point(300,450));
        var res = coreApiClient.getSimRoute(request, null).block();

        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(res));
    }
}