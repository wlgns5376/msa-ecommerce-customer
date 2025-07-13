package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

@Getter
public enum InterestLevel {
    LOW(1, "낮음"),
    MEDIUM(2, "보통"),
    HIGH(3, "높음"),
    VERY_HIGH(4, "매우 높음");

    private final int score;
    private final String displayName;

    InterestLevel(int score, String displayName) {
        this.score = score;
        this.displayName = displayName;
    }
}