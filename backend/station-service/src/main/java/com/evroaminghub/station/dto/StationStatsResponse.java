package com.evroaminghub.station.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class StationStatsResponse {
    private long totalStations;
    private long availableStations;
    private long busyStations;
    private long offlineStations;
    private long totalConnectors;
}
