package com.evroaminghub.auth.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
    private String profilePhoto;
    private String preferredLang;
}
