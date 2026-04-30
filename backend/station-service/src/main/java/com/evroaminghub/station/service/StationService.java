package com.evroaminghub.station.service;

import com.evroaminghub.station.dto.*;
import com.evroaminghub.station.entity.*;
import com.evroaminghub.station.exception.ResourceNotFoundException;
import com.evroaminghub.station.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StationService {

    private final ChargingStationRepository stationRepository;
    private final ConnectorRepository connectorRepository;
    private final CpoNetworkRepository cpoNetworkRepository;

    @Transactional(readOnly = true)
    public Page<StationSummaryResponse> searchStations(
            String city, String status, String connectorType,
            Double lat, Double lng, Double radiusKm, Pageable pageable) {

        if (lat != null && lng != null) {
            // Bounding box search
            double latDelta = radiusKm / 111.0;
            double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));
            return stationRepository.findByLocationBBox(
                    lat - latDelta, lat + latDelta,
                    lng - lngDelta, lng + lngDelta,
                    pageable).map(this::toSummary);
        }

        if (city != null) {
            return stationRepository.findByCityIgnoreCase(city, pageable).map(this::toSummary);
        }

        return stationRepository.findAll(pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public StationDetailResponse getStationById(UUID stationId) {
        ChargingStation station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found: " + stationId));
        return toDetail(station);
    }

    @Transactional
    public StationDetailResponse createStation(CreateStationRequest request) {
        CpoNetwork network = cpoNetworkRepository.findById(request.getCpoNetworkId())
                .orElseThrow(() -> new ResourceNotFoundException("CPO Network not found"));

        ChargingStation station = ChargingStation.builder()
                .cpoNetwork(network)
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .locationType(request.getLocationType())
                .operatingHours(request.getOperatingHours())
                .phone(request.getPhone())
                .status(StationStatus.AVAILABLE)
                .build();

        station = stationRepository.save(station);
        log.info("Created station: {} ({})", station.getName(), station.getId());
        return toDetail(station);
    }

    @Transactional
    public StationDetailResponse updateStation(UUID stationId, UpdateStationRequest request) {
        ChargingStation station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found: " + stationId));

        if (request.getName() != null) station.setName(request.getName());
        if (request.getAddress() != null) station.setAddress(request.getAddress());
        if (request.getStatus() != null) station.setStatus(StationStatus.valueOf(request.getStatus()));
        if (request.getPhone() != null) station.setPhone(request.getPhone());

        return toDetail(stationRepository.save(station));
    }

    @Transactional(readOnly = true)
    public List<ConnectorResponse> getConnectors(UUID stationId) {
        return connectorRepository.findByStationId(stationId)
                .stream().map(this::toConnectorResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StationSummaryResponse> getStationsByNetwork(UUID cpoNetworkId) {
        return stationRepository.findByCpoNetworkId(cpoNetworkId)
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StationStatsResponse getPlatformStats() {
        long total = stationRepository.count();
        long available = stationRepository.countByStatus(StationStatus.AVAILABLE);
        long busy = stationRepository.countByStatus(StationStatus.BUSY);
        long offline = stationRepository.countByStatus(StationStatus.OFFLINE);
        long connectors = connectorRepository.count();

        return StationStatsResponse.builder()
                .totalStations(total)
                .availableStations(available)
                .busyStations(busy)
                .offlineStations(offline)
                .totalConnectors(connectors)
                .build();
    }

    private StationSummaryResponse toSummary(ChargingStation s) {
        long availableConnectors = s.getConnectors() != null
                ? s.getConnectors().stream().filter(c -> "AVAILABLE".equals(c.getStatus().name())).count()
                : 0;

        return StationSummaryResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .city(s.getCity())
                .state(s.getState())
                .address(s.getAddress())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .status(s.getStatus().name())
                .cpoNetworkName(s.getCpoNetwork().getName())
                .cpoNetworkCode(s.getCpoNetwork().getCode())
                .totalConnectors(s.getConnectors() != null ? s.getConnectors().size() : 0)
                .availableConnectors((int) availableConnectors)
                .build();
    }

    private StationDetailResponse toDetail(ChargingStation s) {
        return StationDetailResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .address(s.getAddress())
                .city(s.getCity())
                .state(s.getState())
                .pincode(s.getPincode())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .status(s.getStatus().name())
                .locationType(s.getLocationType())
                .phone(s.getPhone())
                .cpoNetworkId(s.getCpoNetwork().getId())
                .cpoNetworkName(s.getCpoNetwork().getName())
                .cpoNetworkCode(s.getCpoNetwork().getCode())
                .connectors(s.getConnectors() != null
                        ? s.getConnectors().stream().map(this::toConnectorResponse).collect(Collectors.toList())
                        : List.of())
                .lastHeartbeat(s.getLastHeartbeat())
                .build();
    }

    private ConnectorResponse toConnectorResponse(Connector c) {
        return ConnectorResponse.builder()
                .id(c.getId())
                .evseId(c.getEvseId())
                .connectorNumber(c.getConnectorNumber())
                .standard(c.getStandard())
                .powerType(c.getPowerType())
                .maxVoltage(c.getMaxVoltage())
                .maxAmperage(c.getMaxAmperage())
                .maxElectricPower(c.getMaxElectricPower())
                .status(c.getStatus().name())
                .lastStatusUpdate(c.getLastStatusUpdate())
                .build();
    }
}
