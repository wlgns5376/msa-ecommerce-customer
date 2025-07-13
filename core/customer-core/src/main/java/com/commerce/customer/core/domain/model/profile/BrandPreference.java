package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

import java.util.Objects;

@Getter
public class BrandPreference {
    private final String brandId;
    private final String brandName;
    private final PreferenceLevel level;

    private BrandPreference(String brandId, String brandName, PreferenceLevel level) {
        validateBrandId(brandId);
        validateBrandName(brandName);
        
        this.brandId = brandId;
        this.brandName = brandName;
        this.level = Objects.requireNonNull(level, "선호도 레벨은 필수값입니다.");
    }

    public static BrandPreference of(String brandId, String brandName, PreferenceLevel level) {
        return new BrandPreference(brandId, brandName, level);
    }

    private void validateBrandId(String brandId) {
        if (brandId == null || brandId.trim().isEmpty()) {
            throw new IllegalArgumentException("브랜드 ID는 필수값입니다.");
        }
    }

    private void validateBrandName(String brandName) {
        if (brandName == null || brandName.trim().isEmpty()) {
            throw new IllegalArgumentException("브랜드 이름은 필수값입니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrandPreference that = (BrandPreference) o;
        return Objects.equals(brandId, that.brandId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brandId);
    }

    @Override
    public String toString() {
        return "BrandPreference{" +
                "brandId='" + brandId + '\'' +
                ", brandName='" + brandName + '\'' +
                ", level=" + level +
                '}';
    }
}