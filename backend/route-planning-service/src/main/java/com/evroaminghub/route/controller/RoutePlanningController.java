package com.evroaminghub.route.controller;

import com.evroaminghub.route.service.RoutePlanningService;
import com.evroaminghub.route.service.SocEstimationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/route")
@RequiredArgsConstructor
@Tag(name = "Route Planning", description = "EV route optimization and charging stop recommendations")
public class RoutePlanningController {

    private final RoutePlanningService routeService;
    private final SocEstimationService socService;

    @GetMapping("/plan")
    @Operation(summary = "Plan an EV route with optimal charging stops")
    public ResponseEntity<Map<String, Object>> planRoute(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(defaultValue = "80") double batteryLevel,
            @RequestParam(defaultValue = "400") double vehicleRangeKm,
            @RequestParam(defaultValue = "false") boolean avoidTolls) {
        return ResponseEntity.ok(routeService.planRoute(origin, destination, batteryLevel, vehicleRangeKm, avoidTolls));
    }

    @GetMapping("/charging-stops")
    @Operation(summary = "Find nearby charging stops for a location")
    public ResponseEntity<Map<String, Object>> findChargingStops(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double radiusKm,
            @RequestParam(required = false) String connectorType) {
        return ResponseEntity.ok(routeService.findNearbyChargingStops(lat, lng, radiusKm, connectorType));
    }

    @GetMapping("/soc-estimate")
    @Operation(summary = "Estimate battery SOC after traveling a distance")
    public ResponseEntity<Map<String, Object>> estimateSoc(
            @RequestParam double distanceKm,
            @RequestParam double currentSocPercent,
            @RequestParam(defaultValue = "400") double vehicleRangeKm,
            @RequestParam(required = false) String vehicleModel) {
        return ResponseEntity.ok(socService.estimate(distanceKm, currentSocPercent, vehicleRangeKm, vehicleModel));
    }
}
