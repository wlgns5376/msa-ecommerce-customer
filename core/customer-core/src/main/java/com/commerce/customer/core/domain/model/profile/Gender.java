package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

@Getter
public enum Gender {
    MALE("남성"),
    FEMALE("여성"),
    OTHER("기타"),
    PREFER_NOT_TO_SAY("응답하지 않음");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }
}