package com.evroaminghub.roaming.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OcpiResponse<T> {
    private int statusCode;
    private String statusMessage;
    private String timestamp;
    private T data;

    public static <T> OcpiResponse<T> success(T data) {
        return OcpiResponse.<T>builder()
                .statusCode(1000)
                .statusMessage("Success")
                .timestamp(java.time.Instant.now().toString())
                .data(data)
                .build();
    }
}
