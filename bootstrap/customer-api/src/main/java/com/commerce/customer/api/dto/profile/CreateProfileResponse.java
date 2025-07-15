package com.commerce.customer.api.dto.profile;

import lombok.Getter;

@Getter
public class CreateProfileResponse {
    private final Long profileId;
    private final String message;
    
    public CreateProfileResponse(Long profileId, String message) {
        this.profileId = profileId;
        this.message = message;
    }
}