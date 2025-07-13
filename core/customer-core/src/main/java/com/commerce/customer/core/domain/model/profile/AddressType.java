package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

@Getter
public enum AddressType {
    HOME("집"),
    WORK("회사"),
    OTHER("기타");

    private final String displayName;

    AddressType(String displayName) {
        this.displayName = displayName;
    }
}