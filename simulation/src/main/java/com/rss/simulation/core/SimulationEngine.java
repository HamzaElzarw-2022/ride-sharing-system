package com.rss.simulation.core;

import com.rss.simulation.agent.DriverAgent;
import com.rss.simulation.agent.RiderWorkload;
import com.rss.simulation.scenario.Scenario;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class SimulationEngine {
    private final AgentFactory agentFactory;

    public SimulationEngine(AgentFactory agentFactory) {
        this.agentFactory = agentFactory;
    }

    public void run(Scenario scenario) throws InterruptedException {
        System.out.println("[SimulationEngine] Starting scenario: drivers=" + scenario.getDriverCount()
                + ", riders=" + scenario.getRiderCount()
                + ", durationSec=" + scenario.getDurationSeconds()
                + ", timeFactor=" + scenario.getTimeFactor()
                + ", seed=" + scenario.getSeed()
                + ", map=[" + scenario.getMap().getMaxX() + "x" + scenario.getMap().getMaxY() + "]"
                + ", minTripDist=" + scenario.getTrip().getMinDistance()
        );

        Random rng = new Random(scenario.getSeed());

        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            List<DriverAgent> drivers = new ArrayList<>();

            // Keep trying to satisfy required number of drivers
            int target = scenario.getDriverCount();
            long retryDelayMillis = 1000; // 1s backoff between rounds
            int attempt = 0;
            while (drivers.size() < target) {
                int remaining = target - drivers.size();
                attempt++;
                System.out.println("[SimulationEngine] Creating drivers: have=" + drivers.size() + ", need=" + remaining + " (attempt " + attempt + ")");
                for (int i = drivers.size(); i < target; i++) {
                    try {
                        var driver = agentFactory.createDriver(i + 1, new Random(rng.nextLong()), scenario.getMap().getMaxX(), scenario.getMap().getMaxY());
                        if (driver != null) {
                            drivers.add(driver);
                        }
                    } catch (Exception e) {
                        // swallow and retry in next round
                    }
                }
                if (drivers.size() < target) {
                    Thread.sleep(retryDelayMillis);
                    retryDelayMillis *= 2; // Exponential backoff
                }
            }

            RiderWorkload riderWorkload = agentFactory.createRiderWorkload(scenario, new Random(rng.nextLong()));

            List<Callable<Void>> tasks = new ArrayList<>();
            for (DriverAgent d : drivers) {
                tasks.add(() -> { d.run(); return null; });
            }
            tasks.add(() -> { riderWorkload.run(); return null; });

            // If duration is -1 run indefinitely by using invokeAll without timeout.
            if (scenario.getDurationSeconds() == -1) {
                // This will block until all tasks complete (which may be never for an indefinite run)
                System.out.println("[SimulationEngine] Running indefinitely");
                exec.invokeAll(tasks);
            } else {
                exec.invokeAll(tasks, scenario.getDurationSeconds(), TimeUnit.SECONDS);
            }

            // After duration, request stop
            drivers.forEach(DriverAgent::stop);
            riderWorkload.stop();

            // Give them a moment to finish
            Thread.sleep(200);
        }

        System.out.println("[SimulationEngine] Scenario completed.");
    }
}
