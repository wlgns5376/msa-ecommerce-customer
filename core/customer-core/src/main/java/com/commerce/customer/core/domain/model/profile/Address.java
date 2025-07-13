package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

@Getter
public class Address {
    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile("^\\d{5}$");
    
    private final AddressId addressId;
    private final AddressType type;
    private String alias;
    private final String zipCode;
    private final String roadAddress;
    private final String jibunAddress;
    private final String detailAddress;
    private String deliveryMemo;
    private boolean isDefault;
    private final LocalDateTime createdAt;

    private Address(AddressId addressId, AddressType type, String alias, String zipCode, 
                   String roadAddress, String jibunAddress, String detailAddress, String deliveryMemo, 
                   boolean isDefault, LocalDateTime createdAt) {
        this.addressId = Objects.requireNonNull(addressId, "주소 ID는 필수값입니다.");
        this.type = Objects.requireNonNull(type, "주소 타입은 필수값입니다.");
        this.zipCode = Objects.requireNonNull(zipCode, "우편번호는 필수값입니다.");
        this.roadAddress = Objects.requireNonNull(roadAddress, "도로명 주소는 필수값입니다.");
        this.jibunAddress = jibunAddress; // nullable - 지번 주소는 선택사항
        this.detailAddress = detailAddress; // nullable
        this.deliveryMemo = deliveryMemo; // nullable
        this.isDefault = isDefault;
        this.createdAt = Objects.requireNonNull(createdAt, "생성일시는 필수값입니다.");
        
        validateAndSetAlias(alias);
        validateZipCode(zipCode);
        validateRoadAddress(roadAddress);
    }

    public static Address create(AddressType type, String alias, String zipCode, 
                               String roadAddress, String jibunAddress, String detailAddress) {
        return new Address(
            AddressId.generate(),
            type,
            alias,
            zipCode,
            roadAddress,
            jibunAddress,
            detailAddress,
            null,
            false,
            LocalDateTime.now()
        );
    }

    public void updateAlias(String newAlias) {
        validateAndSetAlias(newAlias);
    }

    public void updateDeliveryMemo(String deliveryMemo) {
        this.deliveryMemo = deliveryMemo;
    }

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void removeDefault() {
        this.isDefault = false;
    }

    private void validateAndSetAlias(String alias) {
        if (alias == null || alias.trim().isEmpty()) {
            throw new IllegalArgumentException("주소 별칭은 필수값입니다.");
        }
        if (alias.length() > 50) {
            throw new IllegalArgumentException("주소 별칭은 50자를 초과할 수 없습니다.");
        }
        this.alias = alias;
    }

    private void validateZipCode(String zipCode) {
        if (!ZIP_CODE_PATTERN.matcher(zipCode).matches()) {
            throw new IllegalArgumentException("우편번호는 5자리 숫자여야 합니다.");
        }
    }

    private void validateRoadAddress(String roadAddress) {
        // roadAddress는 이미 Objects.requireNonNull에서 검증됨
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(addressId, address.addressId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressId);
    }

    @Override
    public String toString() {
        return "Address{" +
                "addressId=" + addressId +
                ", type=" + type +
                ", alias='" + alias + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", roadAddress='" + roadAddress + '\'' +
                ", jibunAddress='" + jibunAddress + '\'' +
                ", detailAddress='" + detailAddress + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}