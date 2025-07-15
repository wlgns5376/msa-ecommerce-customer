package com.commerce.customer.core.application.usecase.profile;

import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.profile.ContactInfo;
import com.commerce.customer.core.domain.model.profile.PersonalInfo;
import com.commerce.customer.core.domain.model.profile.ProfileId;

public interface CreateCustomerProfileUseCase {
    ProfileId createProfile(AccountId accountId, PersonalInfo personalInfo, ContactInfo contactInfo);
}