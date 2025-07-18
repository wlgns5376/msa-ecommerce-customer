package com.commerce.customer.core.application.usecase.account;

import com.commerce.customer.core.domain.model.Account;
import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.Email;
import com.commerce.customer.core.domain.model.Password;
import com.commerce.customer.core.domain.model.jwt.TokenPair;

public interface LoginUseCase {
    TokenPair login(Email email, Password password);
    Account getAccount(AccountId accountId);
}