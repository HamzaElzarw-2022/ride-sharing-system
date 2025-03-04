package com.rss.backend.location;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final StringRedisTemplate stringRedisTemplate;

    public void updateDriverLocation(Point coordinates) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        stringRedisTemplate.opsForGeo().add("drivers", coordinates, username);

    }
}
