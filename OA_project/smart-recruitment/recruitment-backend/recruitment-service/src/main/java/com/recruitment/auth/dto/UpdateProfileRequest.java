package com.recruitment.auth.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String realName;
    private String email;
    private String phone;
    private String avatarUrl;
}
