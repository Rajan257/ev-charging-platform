package com.evroaminghub.roaming.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OcpiLocation {
    private String id;
    private String name;
    private String address;
    private String city;
    private String country;
    private Double lat;
    private Double lng;
}
