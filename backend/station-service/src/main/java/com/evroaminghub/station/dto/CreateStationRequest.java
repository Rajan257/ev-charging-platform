package com.evroaminghub.station.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateStationRequest {
    @NotNull private UUID cpoNetworkId;
    @NotBlank private String name;
    @NotBlank private String address;
    @NotBlank private String city;
    @NotBlank private String state;
    @NotBlank private String pincode;
    @NotNull private Double latitude;
    @NotNull private Double longitude;
    private String locationType;
    private String operatingHours;
    private String phone;
}
