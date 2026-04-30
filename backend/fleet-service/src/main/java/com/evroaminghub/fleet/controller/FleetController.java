package com.evroaminghub.fleet.controller;

import com.evroaminghub.fleet.entity.*;
import com.evroaminghub.fleet.repository.*;
import com.evroaminghub.fleet.service.FleetBillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fleets")
@RequiredArgsConstructor
@Tag(name = "Fleet Management", description = "Corporate fleet accounts, drivers, vehicles, and billing")
public class FleetController {

    private final FleetRepository fleetRepository;
    private final FleetDriverRepository driverRepository;
    private final FleetBillingService billingService;

    @PostMapping
    @Operation(summary = "Create a new corporate fleet account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Fleet> createFleet(@RequestBody Fleet fleet) {
        return ResponseEntity.ok(fleetRepository.save(fleet));
    }

    @GetMapping
    @Operation(summary = "List all fleet accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Fleet>> getAllFleets() {
        return ResponseEntity.ok(fleetRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fleet details")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<Fleet> getFleet(@PathVariable String id) {
        return fleetRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/drivers")
    @Operation(summary = "Add a driver to a fleet")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<FleetDriver> addDriver(
            @PathVariable String id,
            @RequestBody FleetDriver driver) {
        driver.setFleetId(id);
        return ResponseEntity.ok(driverRepository.save(driver));
    }

    @GetMapping("/{id}/drivers")
    @Operation(summary = "List all drivers in a fleet")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<List<FleetDriver>> getDrivers(@PathVariable String id) {
        return ResponseEntity.ok(driverRepository.findByFleetId(id));
    }

    @GetMapping("/{id}/invoice/{year}/{month}")
    @Operation(summary = "Get consolidated monthly fleet invoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<Map<String, Object>> getMonthlyInvoice(
            @PathVariable String id,
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(billingService.generateMonthlyInvoice(id, year, month));
    }

    @GetMapping("/{id}/spend-summary")
    @Operation(summary = "Get current month spend vs budget")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<Map<String, Object>> getSpendSummary(@PathVariable String id) {
        return ResponseEntity.ok(billingService.getSpendSummary(id));
    }
}
