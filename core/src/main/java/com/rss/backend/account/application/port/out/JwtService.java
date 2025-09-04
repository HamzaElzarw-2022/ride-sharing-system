package com.rss.backend.account.application.port.out;

import io.jsonwebtoken.Claims;
import java.util.Map;
import java.util.function.Function;

public interface JwtService {
    String extractUsername(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    String generateToken(String username);

    String generateToken(Map<String, Object> extraClaims, String username);

    boolean isTokenValid(String token, String username);
}
