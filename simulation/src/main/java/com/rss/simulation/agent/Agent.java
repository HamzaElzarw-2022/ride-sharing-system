package com.rss.simulation.agent;

public interface Agent extends Runnable {
    void stop();
    String name();
}
