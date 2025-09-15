package com.rss.simulation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sim.driver")
public class IdentityProperties {
    private String defaultPassword = "Passw0rd!";
    private String emailDomain = "sim.rss";

    public String getDefaultPassword() {
        return defaultPassword;
    }
    public void setDefaultPassword(String p) {
        this.defaultPassword = p;
    }
    public String getEmailDomain() {
        return emailDomain;
    }
    public void setEmailDomain(String d) {
        this.emailDomain = d;
    }
}