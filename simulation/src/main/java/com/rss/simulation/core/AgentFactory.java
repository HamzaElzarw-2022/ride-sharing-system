package com.rss.simulation.core;

import com.rss.simulation.agent.DriverAgent;
import com.rss.simulation.agent.Identity;
import com.rss.simulation.agent.Identity.Role;
import com.rss.simulation.agent.IdentityFactory;
import com.rss.simulation.agent.RiderWorkload;
import com.rss.simulation.client.CoreApiClient;
import com.rss.simulation.clock.SimClock;
import com.rss.simulation.scenario.Scenario;
import com.rss.simulation.trip.TripRequestInbox;
import com.rss.simulation.trip.RiderAvailabilityInbox;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class AgentFactory {
    private final SimClock clock;
    private final IdentityFactory identityFactory;
    private final CoreApiClient coreApiClient; // Ideally injected
    private final TripRequestInbox tripRequestInbox;
    private final RiderAvailabilityInbox riderAvailabilityInbox;

    public AgentFactory(IdentityFactory identityFactory, SimClock clock, CoreApiClient coreApiClient, TripRequestInbox tripRequestInbox, RiderAvailabilityInbox riderAvailabilityInbox) {
        this.identityFactory = identityFactory;
        this.clock = clock;
        this.coreApiClient = coreApiClient;
        this.tripRequestInbox = tripRequestInbox;
        this.riderAvailabilityInbox = riderAvailabilityInbox;
    }

    public DriverAgent createDriver(int id, Random rng) {
        var identity = identityFactory.createIdentity(id, Role.DRIVER);
        return identity == null ? null : new DriverAgent(id, clock, rng, identity, coreApiClient, tripRequestInbox);
    }

    public RiderWorkload createRiderWorkload(Scenario scenario, Random rng) {
        List<Identity> identities = new ArrayList<>();
        for (int i = 0; i < scenario.getRiderCount(); i++) {
            identities.add(identityFactory.createIdentity(i, Role.RIDER));
        }

        return new RiderWorkload(clock, coreApiClient, identities, rng, riderAvailabilityInbox);
    }
}
