package com.evroaminghub.roaming.service;

import com.evroaminghub.roaming.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OcpiService {

    public List<OcpiEndpoint> getModuleEndpoints() {
        return List.of(
                OcpiEndpoint.builder().identifier("credentials").role("SENDER").url("http://localhost:8086/ocpi/2.2.1/credentials").build(),
                OcpiEndpoint.builder().identifier("locations").role("SENDER").url("http://localhost:8086/ocpi/2.2.1/locations").build(),
                OcpiEndpoint.builder().identifier("tokens").role("RECEIVER").url("http://localhost:8086/ocpi/2.2.1/tokens").build(),
                OcpiEndpoint.builder().identifier("sessions").role("SENDER").url("http://localhost:8086/ocpi/2.2.1/sessions").build(),
                OcpiEndpoint.builder().identifier("cdrs").role("SENDER").url("http://localhost:8086/ocpi/2.2.1/cdrs").build()
        );
    }

    public OcpiCredentials registerPartner(String token, OcpiCredentials credentials) {
        log.info("Registering roaming partner: {} ({})", credentials.getPartyId(), credentials.getCountryCode());
        return OcpiCredentials.builder()
                .token("ev-roaming-hub-token-" + System.currentTimeMillis())
                .url("http://localhost:8086/ocpi/2.2.1")
                .partyId("EVH")
                .countryCode("IN")
                .build();
    }

    public OcpiCredentials updateCredentials(String token, OcpiCredentials credentials) {
        return registerPartner(token, credentials);
    }

    public List<OcpiLocation> getLocations(int offset, int limit) {
        return List.of(
                OcpiLocation.builder().id("LOC001").name("EV Hub Delhi").address("Connaught Place, New Delhi").city("New Delhi").country("IN").lat(28.6315).lng(77.2167).build(),
                OcpiLocation.builder().id("LOC002").name("EV Hub Mumbai").address("BKC, Mumbai").city("Mumbai").country("IN").lat(19.0607).lng(72.8656).build()
        );
    }

    public OcpiLocation getLocation(String locationId) {
        return OcpiLocation.builder().id(locationId).name("EV Hub " + locationId).address("Sample Address").city("Bangalore").country("IN").lat(12.9716).lng(77.5946).build();
    }

    public OcpiAuthorizationInfo authorizeToken(String uid, String tokenType) {
        log.info("Authorizing roaming token: {} type: {}", uid, tokenType);
        return OcpiAuthorizationInfo.builder().allowed("ALLOWED").token(uid).build();
    }

    public OcpiToken getToken(String uid, String type) {
        return OcpiToken.builder().uid(uid).type(type).valid(true).whitelist("ALWAYS").build();
    }

    public List<Map<String, Object>> getRoamingSessions() {
        return List.of();
    }

    public List<Map<String, Object>> getCdrs() {
        return List.of();
    }
}
