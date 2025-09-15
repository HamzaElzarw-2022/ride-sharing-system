package com.rss.simulation.agent;

import com.rss.simulation.agent.Identity.Role;
import com.rss.simulation.client.CoreApiClient;
import com.rss.simulation.client.dto.AuthRequest;
import com.rss.simulation.client.dto.AuthResponse;
import com.rss.simulation.client.dto.RegisterDriverRequest;
import com.rss.simulation.client.dto.RegisterRiderRequest;
import com.rss.simulation.config.IdentityProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class IdentityFactory {
    private final IdentityProperties identityProperties;
    private final CoreApiClient coreApiClient;
    private final String sessionIdentityId;

    public IdentityFactory(CoreApiClient coreApiClient, IdentityProperties identityProperties) {
        this.coreApiClient = coreApiClient;
        this.identityProperties =identityProperties;
        this.sessionIdentityId = generateRandomLetters(3);
        System.out.println("[IdentityFactory] session id: " + sessionIdentityId);
    }

    public Identity createIdentity(int id, Role role) {
        String username = sessionIdentityId + "-" + role.name().toLowerCase() + "-" + id;
        String email = username + "@" + identityProperties.getEmailDomain();
        String licence = "LIC-" + generateRandomLetters(4).toUpperCase();
        String vehicle = "Vehicle model X";
        var identity = new Identity(id, role, username, email, identityProperties.getDefaultPassword());

        try {
            AuthResponse registerRes;
            if(role.equals(Role.DRIVER)) {
                var registerReq = new RegisterDriverRequest(username, email, identity.getPassword(), licence, vehicle);
                registerRes = coreApiClient.registerDriver(registerReq).block();
            }
            else {
                var registerReq = new RegisterRiderRequest(username, email, identity.getPassword());
                registerRes = coreApiClient.registerRider(registerReq).block();
            }

            if (registerRes != null && registerRes.token() != null) {
                identity.setJwt(registerRes.token());
                identity.setUserId(registerRes.userId());
                identity.setDriverId(registerRes.driverId());
                identity.setRiderId(registerRes.riderId());

                System.out.println("[DriverAgent] registered+authed: " + identity.getEmail());
                return identity;
            }
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 409 || e.getStatusCode().value() == 400) {
                // Already exists or bad request - try to authenticate with known password
                try {
                    var auth = coreApiClient.authenticate(new AuthRequest(identity.getEmail(), identity.getPassword())).block();
                    if (auth != null && auth.token() != null) {
                        identity.setJwt(auth.token());
                        identity.setUserId(auth.userId());
                        identity.setDriverId(auth.driverId());
                        identity.setRiderId(auth.riderId());

                        System.out.println("[DriverAgent] authenticated existing: " + identity.getEmail());
                        return identity;
                    }
                } catch (Exception ie) {
                    // return null
                }
            }
            // other status - return null
        } catch (Exception e) {
            // network or other error - return null
        }

        System.err.println("[DriverAgent] failed to obtain JWT for " + identity.getEmail());
        return null;
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
