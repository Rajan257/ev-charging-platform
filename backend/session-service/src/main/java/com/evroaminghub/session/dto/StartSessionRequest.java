package com.evroaminghub.session.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class StartSessionRequest {
    @NotNull private UUID connectorId;
    @NotNull private UUID stationId;
    private UUID rfidCardId;
    private UUID vehicleId;
    private String authMethod; // APP, RFID, PLUG_AND_CHARGE
}
