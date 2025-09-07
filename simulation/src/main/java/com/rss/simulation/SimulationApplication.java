package com.rss.simulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SimulationApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(SimulationApplication.class);
        app.setAdditionalProfiles("cli");
        app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
        app.run(args);
    }

}
