package com.evroaminghub.device.dto;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ConfigUpdateRequest { private String key; private String value; }
