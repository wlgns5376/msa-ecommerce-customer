package com.commerce.customer.core.domain.model;

import lombok.Getter;

@Getter
public enum AccountStatus {
    PENDING("가입 대기", "이메일 인증 대기 중인 상태"),
    ACTIVE("활성", "정상적으로 사용 가능한 상태"),
    INACTIVE("비활성", "일시적으로 비활성화된 상태"),
    DORMANT("휴면", "장기간 미접속으로 휴면 상태"),
    SUSPENDED("정지", "정책 위반 등으로 정지된 상태"),
    DELETED("탈퇴", "사용자가 탈퇴한 상태");

    private final String displayName;
    private final String description;

    AccountStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean canLogin() {
        return this == ACTIVE;
    }

    public boolean canActivate() {
        return this == PENDING || this == INACTIVE || this == DORMANT;
    }

    public boolean canDeactivate() {
        return this == ACTIVE;
    }

    public boolean canDelete() {
        return this != DELETED;
    }
}