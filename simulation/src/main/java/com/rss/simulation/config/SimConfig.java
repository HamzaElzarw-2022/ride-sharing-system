package com.rss.simulation.config;

import com.rss.simulation.clock.AcceleratedClock;
import com.rss.simulation.clock.RealTimeClock;
import com.rss.simulation.clock.SimClock;
import com.rss.simulation.scenario.ScenarioLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimConfig {

    @Bean
    public SimClock simClock(ScenarioLoader loader) {
        double factor = loader.getScenario().getTimeFactor();
        if (factor <= 1.0) {
            return new RealTimeClock();
        }
        return new AcceleratedClock(factor);
    }
}
