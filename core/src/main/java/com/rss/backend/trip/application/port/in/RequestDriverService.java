package com.rss.backend.trip.application.port.in;

import com.rss.backend.trip.domain.entity.Trip;

public interface RequestDriverService {

    /*
    * This method should find the nearest available drivers to the user's location and send requests to them.
    * As drivers are running by the simulation service,
    * I should find a way to notify the simulation service that a request has been created for a driver.
    * maybe using Redis pub/sub or a message queue like RabbitMQ.
    *
    * Also should keep track of requests sent to drivers temporarily, maybe in Redis or in-memory data structure,
    * if a driver does not respond within a certain time frame. Either mark trip as CANCELED or send request to another driver.
    * */
    void requestDriver(Trip trip);

    /*
    * This method checks if a driver was requested for a specific trip.
    * */
    boolean isDriverRequestedForTrip(Long driverId, Long tripId);
}
