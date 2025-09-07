package com.rss.core.trip.application.port.out;

import java.util.List;

public interface NotificationService {

    void NotifyDriverRequest(Long driverId, Long tripId);
    void NotifyDriverRequest(List<Long> driverIds, Long tripId);
}
