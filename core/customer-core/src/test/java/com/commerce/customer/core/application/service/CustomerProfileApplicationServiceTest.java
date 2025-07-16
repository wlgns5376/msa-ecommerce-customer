package com.commerce.customer.core.application.service;

import com.commerce.customer.core.domain.model.Account;
import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.profile.Address;
import com.commerce.customer.core.domain.model.profile.AddressId;
import com.commerce.customer.core.domain.model.profile.AddressType;
import com.commerce.customer.core.domain.model.profile.BirthDate;
import com.commerce.customer.core.domain.model.profile.ContactInfo;
import com.commerce.customer.core.domain.model.profile.CustomerProfile;
import com.commerce.customer.core.domain.model.profile.FullName;
import com.commerce.customer.core.domain.model.profile.Gender;
import com.commerce.customer.core.domain.model.profile.PersonalInfo;
import com.commerce.customer.core.domain.model.profile.PhoneNumber;
import com.commerce.customer.core.domain.model.profile.ProfileId;
import com.commerce.customer.core.domain.model.profile.ProfilePreferences;
import com.commerce.customer.core.domain.repository.AccountRepository;
import com.commerce.customer.core.domain.repository.profile.CustomerProfileRepository;
import com.commerce.customer.core.domain.service.profile.CustomerProfileDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerProfileApplicationService 테스트")
class CustomerProfileApplicationServiceTest {

    @Mock
    private CustomerProfileRepository customerProfileRepository;

    @Mock
    private CustomerProfileDomainService customerProfileDomainService;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CustomerProfileApplicationService customerProfileApplicationService;

    private AccountId accountId;
    private CustomerId customerId;
    private ProfileId profileId;
    private Account account;
    private CustomerProfile profile;
    private PersonalInfo personalInfo;
    private ContactInfo contactInfo;

    @BeforeEach
    void setUp() {
        accountId = AccountId.of(1L);
        customerId = CustomerId.of(12345L);
        profileId = ProfileId.of(1L);
        account = mock(Account.class);
        profile = mock(CustomerProfile.class);
        
        FullName fullName = FullName.of("홍", "길동");
        BirthDate birthDate = BirthDate.of(LocalDate.of(1990, 1, 1));
        personalInfo = PersonalInfo.of(fullName, birthDate, Gender.MALE);
        
        PhoneNumber phoneNumber = PhoneNumber.ofKorean("010-1234-5678");
        contactInfo = ContactInfo.of(phoneNumber);
    }

    @Test
    @DisplayName("프로필 생성 성공")
    void createProfile_Success() {
        // given
        given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
        given(account.getCustomerId()).willReturn(customerId);
        given(customerProfileDomainService.createProfile(customerId, personalInfo, contactInfo))
                .willReturn(profile);
        given(profile.getProfileId()).willReturn(profileId);

        // when
        ProfileId result = customerProfileApplicationService.createProfile(accountId, personalInfo, contactInfo);

        // then
        assertThat(result).isEqualTo(profileId);
        then(accountRepository).should().findById(accountId);
        then(customerProfileDomainService).should().createProfile(customerId, personalInfo, contactInfo);
    }

    @Test
    @DisplayName("프로필 생성 실패 - 존재하지 않는 계정")
    void createProfile_AccountNotFound() {
        // given
        given(accountRepository.findById(accountId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerProfileApplicationService.createProfile(accountId, personalInfo, contactInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("계정을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("프로필 조회 성공")
    void getProfile_Success() {
        // given
        given(customerProfileRepository.findById(profileId)).willReturn(Optional.of(profile));

        // when
        CustomerProfile result = customerProfileApplicationService.getProfile(profileId);

        // then
        assertThat(result).isEqualTo(profile);
        then(customerProfileRepository).should().findById(profileId);
    }

    @Test
    @DisplayName("프로필 조회 실패 - 존재하지 않는 프로필")
    void getProfile_NotFound() {
        // given
        given(customerProfileRepository.findById(profileId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerProfileApplicationService.getProfile(profileId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("프로필을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("계정 ID로 프로필 조회 성공")
    void getProfileByAccountId_Success() {
        // given
        given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
        given(account.getCustomerId()).willReturn(customerId);
        given(customerProfileRepository.findByCustomerId(customerId)).willReturn(Optional.of(profile));

        // when
        CustomerProfile result = customerProfileApplicationService.getProfileByAccountId(accountId);

        // then
        assertThat(result).isEqualTo(profile);
        then(accountRepository).should().findById(accountId);
        then(customerProfileRepository).should().findByCustomerId(customerId);
    }

    @Test
    @DisplayName("계정 ID로 프로필 조회 실패 - 존재하지 않는 계정")
    void getProfileByAccountId_AccountNotFound() {
        // given
        given(accountRepository.findById(accountId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerProfileApplicationService.getProfileByAccountId(accountId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("계정을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("계정 ID로 프로필 조회 실패 - 존재하지 않는 프로필")
    void getProfileByAccountId_ProfileNotFound() {
        // given
        given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
        given(account.getCustomerId()).willReturn(customerId);
        given(customerProfileRepository.findByCustomerId(customerId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerProfileApplicationService.getProfileByAccountId(accountId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("계정의 프로필을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("개인정보 업데이트 성공")
    void updatePersonalInfo_Success() {
        // given
        PersonalInfo newPersonalInfo = PersonalInfo.of(FullName.of("김", "철수"));

        // when
        customerProfileApplicationService.updatePersonalInfo(profileId, newPersonalInfo);

        // then
        then(customerProfileDomainService).should().updatePersonalInfo(profileId, newPersonalInfo);
    }

    @Test
    @DisplayName("연락처 정보 업데이트 성공")
    void updateContactInfo_Success() {
        // given
        ContactInfo newContactInfo = ContactInfo.of(PhoneNumber.ofKorean("010-9876-5432"));
        given(customerProfileRepository.findById(profileId)).willReturn(Optional.of(profile));

        // when
        customerProfileApplicationService.updateContactInfo(profileId, newContactInfo);

        // then
        then(customerProfileRepository).should().findById(profileId);
        then(profile).should().updateContactInfo(newContactInfo);
        then(customerProfileRepository).should().save(profile);
    }

    @Test
    @DisplayName("주소 추가 성공")
    void addAddress_Success() {
        // given
        Address address = Address.create(AddressType.HOME, "집", "12345", "서울특별시 강남구 테헤란로 123", null, "상세주소");

        // when
        customerProfileApplicationService.addAddress(profileId, address);

        // then
        then(customerProfileDomainService).should().validateAndAddAddress(profileId, address);
    }

    @Test
    @DisplayName("주소 수정 성공")
    void updateAddress_Success() {
        // given
        Address address = Address.create(AddressType.HOME, "집", "12345", "서울특별시 강남구 테헤란로 123", null, "상세주소");
        given(customerProfileRepository.findById(profileId)).willReturn(Optional.of(profile));

        // when
        customerProfileApplicationService.updateAddress(profileId, address);

        // then
        then(customerProfileRepository).should().findById(profileId);
        then(profile).should().removeAddress(address.getAddressId());
        then(profile).should().addAddress(address);
        then(customerProfileRepository).should().save(profile);
    }

    @Test
    @DisplayName("주소 삭제 성공")
    void removeAddress_Success() {
        // given
        Address address = Address.create(AddressType.HOME, "집", "12345", "서울특별시 강남구 테헤란로 123", null, "상세주소");
        given(customerProfileRepository.findById(profileId)).willReturn(Optional.of(profile));

        // when
        customerProfileApplicationService.removeAddress(profileId, address);

        // then
        then(customerProfileRepository).should().findById(profileId);
        then(profile).should().removeAddress(address.getAddressId());
        then(customerProfileRepository).should().save(profile);
    }

    @Test
    @DisplayName("선호도 업데이트 성공")
    void updatePreferences_Success() {
        // given
        ProfilePreferences preferences = ProfilePreferences.getDefault();

        // when
        customerProfileApplicationService.updatePreferences(profileId, preferences);

        // then
        then(customerProfileDomainService).should().updatePreferences(profileId, preferences);
    }
}