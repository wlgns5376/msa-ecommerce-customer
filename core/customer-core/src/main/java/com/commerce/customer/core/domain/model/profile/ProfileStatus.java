package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

@Getter
public enum ProfileStatus {
    ACTIVE("활성", "정상적으로 사용 가능한 상태"),
    INACTIVE("비활성", "일시적으로 비활성화된 상태"),
    SUSPENDED("정지", "정책 위반 등으로 정지된 상태");

    private final String displayName;
    private final String description;

    ProfileStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean canUpdate() {
        return this == ACTIVE;
    }

    public boolean canAddAddress() {
        return this == ACTIVE;
    }
}