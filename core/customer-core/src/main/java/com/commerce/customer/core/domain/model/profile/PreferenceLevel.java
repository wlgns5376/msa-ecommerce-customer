package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

@Getter
public enum PreferenceLevel {
    DISLIKE(-1, "싫어함"),
    NEUTRAL(0, "보통"),
    LIKE(1, "좋아함"),
    LOVE(2, "매우 좋아함");

    private final int score;
    private final String displayName;

    PreferenceLevel(int score, String displayName) {
        this.score = score;
        this.displayName = displayName;
    }
}