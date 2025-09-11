package com.rss.simulation.core;

import com.rss.simulation.agent.DriverAgent;
import com.rss.simulation.agent.IdentityFactory;
import com.rss.simulation.agent.RiderWorkload;
import com.rss.simulation.client.CoreApiClient;
import com.rss.simulation.clock.SimClock;
import com.rss.simulation.scenario.Scenario;
import com.rss.simulation.trip.TripRequestInbox;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class AgentFactory {
    private final SimClock clock;
    private final IdentityFactory identityFactory;
    private final CoreApiClient coreApiClient; // Ideally injected
    private final TripRequestInbox tripRequestInbox;

    public AgentFactory(IdentityFactory identityFactory, SimClock clock, CoreApiClient coreApiClient, TripRequestInbox tripRequestInbox) {
        this.identityFactory = identityFactory;
        this.clock = clock;
        this.coreApiClient = coreApiClient;
        this.tripRequestInbox = tripRequestInbox;
    }

    public DriverAgent createDriver(int id, Random rng) {
        var identity = identityFactory.createDriverIdentity(id);
        return identity == null ? null : new DriverAgent(id, clock, rng, identity, coreApiClient, tripRequestInbox);
    }

    public RiderWorkload createRiderWorkload(Scenario scenario, Random rng) {
        return new RiderWorkload(clock, scenario.getRiderRequestsPerSecond(), rng);
    }
}
