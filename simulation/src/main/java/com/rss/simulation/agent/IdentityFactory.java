package com.rss.simulation.agent;

import com.rss.simulation.client.CoreApiClient;
import com.rss.simulation.client.dto.AuthRequest;
import com.rss.simulation.client.dto.RegisterDriverRequest;
import com.rss.simulation.config.IdentityProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class IdentityFactory {
    private final IdentityProperties identityProperties;
    private final CoreApiClient coreApiClient;

    public IdentityFactory(CoreApiClient coreApiClient, IdentityProperties identityProperties) {
        this.coreApiClient = coreApiClient;
        this.identityProperties =identityProperties;
    }

    public DriverIdentity createDriverIdentity(int id) {
        var identity = new DriverIdentity(
                "sim-driver-" + id,
                "driver-" + id + "@" + identityProperties.getEmailDomain(),
                identityProperties.getDefaultPassword()
        );

        try {
            var regReq = new RegisterDriverRequest(
                    identity.getUsername(), identity.getEmail(), identity.getPassword(),
                    "LIC-" + id, "Vehicle model X"
            );
            var reg = coreApiClient.registerDriver(regReq).block();
            if (reg != null && reg.token() != null) {
                identity.setJwt(reg.token());
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
}
