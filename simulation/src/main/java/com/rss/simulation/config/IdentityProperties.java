package com.rss.simulation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sim.driver")
public class IdentityProperties {
    private String defaultPassword = "Passw0rd!";
    private String emailDomain = "sim.rss";
    private String sessionId = generateRandomLetters(3);

    public String getDefaultPassword() {
        return defaultPassword;
    }
    public void setDefaultPassword(String p) {
        this.defaultPassword = p;
    }
    public String getEmailDomain() {
        return emailDomain + '.' + sessionId;
    }
    public void setEmailDomain(String d) {
        this.emailDomain = d;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String s) {
        this.sessionId = s;
    }

    private String generateRandomLetters(int length) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}