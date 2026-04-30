package com.evroaminghub.station.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class StationSummaryResponse {
    private UUID id;
    private String name;
    private String city;
    private String state;
    private String address;
    private Double latitude;
    private Double longitude;
    private String status;
    private String cpoNetworkName;
    private String cpoNetworkCode;
    private int totalConnectors;
    private int availableConnectors;
}
