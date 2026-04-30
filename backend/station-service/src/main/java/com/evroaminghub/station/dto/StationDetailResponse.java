package com.evroaminghub.station.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class StationDetailResponse {
    private UUID id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private String status;
    private String locationType;
    private String phone;
    private UUID cpoNetworkId;
    private String cpoNetworkName;
    private String cpoNetworkCode;
    private List<ConnectorResponse> connectors;
    private Instant lastHeartbeat;
}
