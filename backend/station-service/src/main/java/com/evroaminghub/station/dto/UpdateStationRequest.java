package com.evroaminghub.station.dto;

import lombok.Data;

@Data
public class UpdateStationRequest {
    private String name;
    private String address;
    private String phone;
    private String status;
}
