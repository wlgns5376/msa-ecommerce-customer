package com.commerce.customer.core.application.usecase.account;

import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.Email;
import com.commerce.customer.core.domain.model.Password;

public interface CreateAccountUseCase {
    AccountId createAccount(Email email, Password password);
}