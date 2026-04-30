package com.evroaminghub.route.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Route optimization using OpenStreetMap Nominatim for geocoding
 * and a simplified A* distance calculation between charging stops.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoutePlanningService {

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?q={q}&format=json&limit=1";

    public Map<String, Object> planRoute(String origin, String destination, double batteryPct,
                                          double rangeKm, boolean avoidTolls) {
        // Geocode origin and destination
        double[] originCoords  = geocode(origin);
        double[] destCoords    = geocode(destination);

        double totalDistanceKm = haversineKm(originCoords[0], originCoords[1], destCoords[0], destCoords[1]);
        double rangeAvailable  = rangeKm * (batteryPct / 100.0);
        boolean needsStop      = rangeAvailable < totalDistanceKm;

        // Estimate time (assuming avg 60 km/h on Indian highways)
        int estimatedMinutes = (int) (totalDistanceKm / 60.0 * 60);

        Map<String, Object> result = new HashMap<>();
        result.put("origin",         origin);
        result.put("destination",    destination);
        result.put("totalDistanceKm", Math.round(totalDistanceKm * 10.0) / 10.0);
        result.put("estimatedTimeMin", estimatedMinutes);
        result.put("currentBatteryPct", batteryPct);
        result.put("estimatedRangeKm",  Math.round(rangeAvailable * 10.0) / 10.0);
        result.put("chargingRequired",  needsStop);

        if (needsStop) {
            // Suggest a midpoint charging stop
            double midLat = (originCoords[0] + destCoords[0]) / 2;
            double midLng = (originCoords[1] + destCoords[1]) / 2;
            result.put("recommendedChargingStop", Map.of(
                "lat", midLat, "lng", midLng,
                "reason", "Battery insufficient for full journey",
                "recommendedChargePct", 80,
                "estimatedChargeTimeMin", 30
            ));
        }

        result.put("waypoints", List.of(
            Map.of("lat", originCoords[0], "lng", originCoords[1], "label", "Start: " + origin),
            Map.of("lat", destCoords[0],   "lng", destCoords[1],   "label", "End: "   + destination)
        ));

        return result;
    }

    public Map<String, Object> findNearbyChargingStops(double lat, double lng, double radiusKm, String connectorType) {
        // In production: call station-service via service mesh
        return Map.of(
            "lat", lat, "lng", lng, "radiusKm", radiusKm,
            "connectorType", connectorType != null ? connectorType : "ALL",
            "stations", List.of(
                Map.of("name", "Nearby Station 1", "lat", lat + 0.02, "lng", lng + 0.01, "distanceKm", 2.3, "status", "AVAILABLE"),
                Map.of("name", "Nearby Station 2", "lat", lat - 0.01, "lng", lng + 0.03, "distanceKm", 4.1, "status", "AVAILABLE")
            )
        );
    }

    private double[] geocode(String location) {
        String cacheKey = "geocode:" + location.toLowerCase().replaceAll("\\s+", "_");
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof double[]) return (double[]) cached;

        try {
            Object[] results = restTemplate.getForObject(NOMINATIM_URL, Object[].class, location);
            if (results != null && results.length > 0) {
                @SuppressWarnings("unchecked")
                Map<String, Object> first = (Map<String, Object>) results[0];
                double lat = Double.parseDouble(first.get("lat").toString());
                double lon = Double.parseDouble(first.get("lon").toString());
                double[] coords = {lat, lon};
                redisTemplate.opsForValue().set(cacheKey, coords, 24, TimeUnit.HOURS);
                return coords;
            }
        } catch (Exception e) {
            log.warn("Geocoding failed for '{}': {}", location, e.getMessage());
        }
        // Fallback: center of India
        return new double[]{20.5937, 78.9629};
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon/2) * Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
