package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

import java.util.Objects;

@Getter
public class ProfileImage {
    private final String imageUrl;
    private final String altText;

    private ProfileImage(String imageUrl, String altText) {
        validateImageUrl(imageUrl);
        this.imageUrl = imageUrl;
        this.altText = altText != null ? altText : "";
    }

    public static ProfileImage of(String imageUrl, String altText) {
        return new ProfileImage(imageUrl, altText);
    }

    public static ProfileImage of(String imageUrl) {
        return new ProfileImage(imageUrl, null);
    }

    public ProfileImage updateUrl(String newImageUrl) {
        return new ProfileImage(newImageUrl, this.altText);
    }

    public ProfileImage updateAltText(String newAltText) {
        return new ProfileImage(this.imageUrl, newAltText);
    }

    private void validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("프로필 이미지 URL은 필수값입니다.");
        }
        
        if (!imageUrl.matches("^https?://.*\\.(jpg|jpeg|png|gif|webp)$")) {
            throw new IllegalArgumentException("지원되지 않는 이미지 형식입니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileImage that = (ProfileImage) o;
        return Objects.equals(imageUrl, that.imageUrl) &&
               Objects.equals(altText, that.altText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageUrl, altText);
    }

    @Override
    public String toString() {
        return "ProfileImage{" +
                "imageUrl='" + imageUrl + '\'' +
                ", altText='" + altText + '\'' +
                '}';
    }
}