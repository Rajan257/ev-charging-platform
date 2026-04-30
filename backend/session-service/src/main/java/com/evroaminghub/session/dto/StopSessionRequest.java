package com.evroaminghub.session.dto;
import lombok.Data;

@Data
public class StopSessionRequest {
    private String reason; // USER_REQUEST, EMERGENCY, OVERSTAY
}
