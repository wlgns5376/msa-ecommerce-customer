package com.commerce.customer.core.application.usecase.profile;

import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.profile.CustomerProfile;
import com.commerce.customer.core.domain.model.profile.ProfileId;

public interface GetCustomerProfileUseCase {
    CustomerProfile getProfile(ProfileId profileId);
    CustomerProfile getProfileByAccountId(AccountId accountId);
}