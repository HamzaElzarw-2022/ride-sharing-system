package com.rss.simulation.scenario;

import org.springframework.stereotype.Component;

@Component
public class ScenarioLoader {
    private final Scenario scenario;

    public ScenarioLoader(Scenario scenario) {
        this.scenario = scenario;
    }

    public Scenario load() {
        return scenario;
    }

    public Scenario getScenario() {
        return scenario;
    }
}
