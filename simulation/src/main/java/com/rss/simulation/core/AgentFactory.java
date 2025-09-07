package com.rss.simulation.core;

import com.rss.simulation.agent.DriverAgent;
import com.rss.simulation.agent.RiderWorkload;
import com.rss.simulation.clock.SimClock;
import com.rss.simulation.scenario.Scenario;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class AgentFactory {
    private final SimClock clock;

    public AgentFactory(SimClock clock) {
        this.clock = clock;
    }

    public DriverAgent createDriver(int id, Random rng) {
        return new DriverAgent(id, clock, rng);
    }

    public RiderWorkload createRiderWorkload(Scenario scenario, Random rng) {
        return new RiderWorkload(clock, scenario.getRiderRequestsPerSecond(), rng);
    }
}
