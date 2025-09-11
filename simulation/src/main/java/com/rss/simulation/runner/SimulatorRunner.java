package com.rss.simulation.runner;

import com.rss.simulation.core.SimulationEngine;
import com.rss.simulation.scenario.Scenario;
import com.rss.simulation.scenario.ScenarioLoader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("cli")
public class SimulatorRunner implements CommandLineRunner {

    private final ScenarioLoader loader;
    private final SimulationEngine engine;

    public SimulatorRunner(ScenarioLoader loader, SimulationEngine engine) {
        this.loader = loader;
        this.engine = engine;
    }

    @Override
    public void run(String... args) throws Exception {
        Scenario scenario = loader.load();
        engine.run(scenario);
    }
}
