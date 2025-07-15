package com.commerce.customer.core.application.usecase.profile;

import com.commerce.customer.core.domain.model.profile.Address;
import com.commerce.customer.core.domain.model.profile.ContactInfo;
import com.commerce.customer.core.domain.model.profile.PersonalInfo;
import com.commerce.customer.core.domain.model.profile.ProfileId;
import com.commerce.customer.core.domain.model.profile.ProfilePreferences;

public interface UpdateCustomerProfileUseCase {
    void updatePersonalInfo(ProfileId profileId, PersonalInfo personalInfo);
    void updateContactInfo(ProfileId profileId, ContactInfo contactInfo);
    void addAddress(ProfileId profileId, Address address);
    void updateAddress(ProfileId profileId, Address address);
    void removeAddress(ProfileId profileId, Address address);
    void updatePreferences(ProfileId profileId, ProfilePreferences preferences);
}