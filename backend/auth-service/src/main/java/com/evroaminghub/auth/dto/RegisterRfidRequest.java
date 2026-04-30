package com.evroaminghub.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRfidRequest {
    @NotBlank
    private String uid;
    private String label;
    private String cardType; // RFID, NFC, APP
}
