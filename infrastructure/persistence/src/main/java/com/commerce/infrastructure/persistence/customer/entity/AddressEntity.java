package com.commerce.infrastructure.persistence.customer.entity;

import com.commerce.infrastructure.persistence.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses", indexes = {
    @Index(name = "idx_address_profile_id", columnList = "profile_id"),
    @Index(name = "idx_address_type", columnList = "type"),
    @Index(name = "idx_address_default", columnList = "is_default")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private CustomerProfileEntity customerProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private AddressType type;

    @Column(name = "alias", length = 50)
    private String alias;

    @Column(name = "zip_code", nullable = false, length = 10)
    private String zipCode;

    @Column(name = "road_address", nullable = false, length = 200)
    private String roadAddress;

    @Column(name = "jibun_address", length = 200)
    private String jibunAddress;

    @Column(name = "detail_address", length = 100)
    private String detailAddress;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Builder
    public AddressEntity(CustomerProfileEntity customerProfile, AddressType type, String alias,
                        String zipCode, String roadAddress, String jibunAddress, 
                        String detailAddress, Boolean isDefault) {
        this.customerProfile = customerProfile;
        this.type = type;
        this.alias = alias;
        this.zipCode = zipCode;
        this.roadAddress = roadAddress;
        this.jibunAddress = jibunAddress;
        this.detailAddress = detailAddress;
        this.isDefault = isDefault != null ? isDefault : false;
    }

    public void updateAddress(AddressType type, String alias, String zipCode, 
                            String roadAddress, String jibunAddress, String detailAddress) {
        this.type = type;
        this.alias = alias;
        this.zipCode = zipCode;
        this.roadAddress = roadAddress;
        this.jibunAddress = jibunAddress;
        this.detailAddress = detailAddress;
    }

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unsetDefault() {
        this.isDefault = false;
    }

    // JPA를 위한 setter (package-private)
    void setCustomerProfile(CustomerProfileEntity customerProfile) {
        this.customerProfile = customerProfile;
    }

    public enum AddressType {
        HOME, WORK, OTHER
    }
}