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
import java.util.concurrent.Future;

@Component
public class SimulationEngine {
    private final AgentFactory agentFactory;

    public SimulationEngine(AgentFactory agentFactory) {
        this.agentFactory = agentFactory;
    }

    public void run(Scenario scenario) throws InterruptedException {
        System.out.println("[SimulationEngine] Starting scenario: drivers=" + scenario.getDriverCount()
                + ", rps=" + scenario.getRiderRequestsPerSecond()
                + ", durationSec=" + scenario.getDurationSeconds()
                + ", timeFactor=" + scenario.getTimeFactor());

        Random rng = new Random(scenario.getSeed());

        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            List<DriverAgent> drivers = new ArrayList<>();
            for (int i = 0; i < scenario.getDriverCount(); i++) {
                drivers.add(agentFactory.createDriver(i + 1, new Random(rng.nextLong())));
            }
            RiderWorkload riderWorkload = agentFactory.createRiderWorkload(scenario, new Random(rng.nextLong()));

            List<Callable<Void>> tasks = new ArrayList<>();
            for (DriverAgent d : drivers) {
                tasks.add(() -> { d.run(); return null; });
            }
            tasks.add(() -> { riderWorkload.run(); return null; });

            List<Future<Void>> futures = exec.invokeAll(tasks, scenario.getDurationSeconds(), java.util.concurrent.TimeUnit.SECONDS);

            // After duration, request stop
            drivers.forEach(DriverAgent::stop);
            riderWorkload.stop();

            // Give them a moment to finish
            Thread.sleep(200);
        }

        System.out.println("[SimulationEngine] Scenario completed.");
    }
}
