package com.commerce.customer.core.application.usecase.account;

import com.commerce.customer.core.domain.model.jwt.TokenPair;

public interface RefreshTokenUseCase {
    TokenPair refreshToken(String refreshToken);
}