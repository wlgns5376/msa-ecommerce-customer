package com.commerce.customer.core.domain.model.profile;

import com.commerce.customer.core.domain.model.CustomerId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class CustomerProfile {
    private static final int MAX_ADDRESSES = 10;
    
    private final ProfileId profileId;
    private final CustomerId customerId;
    private PersonalInfo personalInfo;
    private ContactInfo contactInfo;
    private final List<Address> addresses;
    private ProfilePreferences preferences;
    private ProfileStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private final List<Object> domainEvents = new ArrayList<>();

    private CustomerProfile(ProfileId profileId, CustomerId customerId, PersonalInfo personalInfo,
                          ContactInfo contactInfo, ProfilePreferences preferences, ProfileStatus status,
                          LocalDateTime createdAt) {
        this.profileId = Objects.requireNonNull(profileId, "프로필 ID는 필수값입니다.");
        this.customerId = Objects.requireNonNull(customerId, "고객 ID는 필수값입니다.");
        this.personalInfo = Objects.requireNonNull(personalInfo, "개인정보는 필수값입니다.");
        this.contactInfo = Objects.requireNonNull(contactInfo, "연락처 정보는 필수값입니다.");
        this.preferences = preferences != null ? preferences : ProfilePreferences.getDefault();
        this.status = Objects.requireNonNull(status, "프로필 상태는 필수값입니다.");
        this.createdAt = Objects.requireNonNull(createdAt, "생성일시는 필수값입니다.");
        this.updatedAt = createdAt;
        this.addresses = new ArrayList<>();
        
        // 도메인 이벤트 발행 (구현 예정)
        // addDomainEvent(new ProfileCreatedEvent(profileId, customerId));
    }

    public static CustomerProfile create(CustomerId customerId, PersonalInfo personalInfo, ContactInfo contactInfo) {
        ProfileId profileId = ProfileId.generate();
        LocalDateTime now = LocalDateTime.now();
        
        return new CustomerProfile(
            profileId,
            customerId,
            personalInfo,
            contactInfo,
            ProfilePreferences.getDefault(),
            ProfileStatus.ACTIVE,
            now
        );
    }

    public void updatePersonalInfo(PersonalInfo newPersonalInfo) {
        validateActiveStatus();
        this.personalInfo = Objects.requireNonNull(newPersonalInfo, "개인정보는 필수값입니다.");
        this.updatedAt = LocalDateTime.now();
        
        // 도메인 이벤트 발행 (구현 예정)
        // addDomainEvent(new ProfileUpdatedEvent(profileId, customerId));
    }

    public void updateContactInfo(ContactInfo newContactInfo) {
        validateActiveStatus();
        this.contactInfo = Objects.requireNonNull(newContactInfo, "연락처 정보는 필수값입니다.");
        this.updatedAt = LocalDateTime.now();
    }

    public void addAddress(Address address) {
        validateActiveStatus();
        validateMaxAddresses();
        
        Objects.requireNonNull(address, "주소는 필수값입니다.");
        
        // 첫 번째 주소는 자동으로 기본 주소가 됨
        if (addresses.isEmpty()) {
            address.setAsDefault();
        }
        
        addresses.add(address);
        this.updatedAt = LocalDateTime.now();
        
        // 도메인 이벤트 발행 (구현 예정)
        // addDomainEvent(new AddressAddedEvent(profileId, address.getAddressId()));
    }

    public void removeAddress(AddressId addressId) {
        validateActiveStatus();
        
        Address addressToRemove = findAddressById(addressId);
        validateMinimumAddresses();
        
        addresses.remove(addressToRemove);
        
        // 삭제된 주소가 기본 주소였다면 다른 주소를 기본으로 설정
        if (addressToRemove.isDefault() && !addresses.isEmpty()) {
            addresses.get(0).setAsDefault();
        }
        
        this.updatedAt = LocalDateTime.now();
        
        // 도메인 이벤트 발행 (구현 예정)
        // addDomainEvent(new AddressRemovedEvent(profileId, addressId));
    }

    public void setDefaultAddress(AddressId addressId) {
        validateActiveStatus();
        
        Address newDefaultAddress = findAddressById(addressId);
        
        // 기존 기본 주소 해제
        addresses.stream()
            .filter(Address::isDefault)
            .forEach(Address::removeDefault);
        
        // 새 기본 주소 설정
        newDefaultAddress.setAsDefault();
        this.updatedAt = LocalDateTime.now();
        
        // 도메인 이벤트 발행 (구현 예정)
        // addDomainEvent(new DefaultAddressChangedEvent(profileId, addressId));
    }

    public void updatePreferences(ProfilePreferences newPreferences) {
        validateActiveStatus();
        this.preferences = Objects.requireNonNull(newPreferences, "선호도 정보는 필수값입니다.");
        this.updatedAt = LocalDateTime.now();
        
        // 도메인 이벤트 발행 (구현 예정)
        // addDomainEvent(new PreferencesUpdatedEvent(profileId, customerId));
    }

    public void activate() {
        if (status != ProfileStatus.INACTIVE) {
            throw new IllegalStateException("비활성 상태에서만 활성화할 수 있습니다.");
        }
        this.status = ProfileStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (status != ProfileStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태에서만 비활성화할 수 있습니다.");
        }
        this.status = ProfileStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateActiveStatus() {
        if (!status.canUpdate()) {
            throw new IllegalStateException("활성 상태에서만 수정할 수 있습니다: " + status);
        }
    }

    private void validateMaxAddresses() {
        if (addresses.size() >= MAX_ADDRESSES) {
            throw new IllegalStateException("주소는 최대 " + MAX_ADDRESSES + "개까지 등록 가능합니다.");
        }
    }

    private void validateMinimumAddresses() {
        if (addresses.size() <= 1) {
            throw new IllegalStateException("최소 하나의 주소는 유지되어야 합니다.");
        }
    }

    private Address findAddressById(AddressId addressId) {
        return addresses.stream()
            .filter(address -> address.getAddressId().equals(addressId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주소입니다."));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerProfile that = (CustomerProfile) o;
        return Objects.equals(profileId, that.profileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId);
    }

    @Override
    public String toString() {
        return "CustomerProfile{" +
                "profileId=" + profileId +
                ", customerId=" + customerId +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}