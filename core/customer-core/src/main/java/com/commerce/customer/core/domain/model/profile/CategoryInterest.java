package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

import java.util.Objects;

@Getter
public class CategoryInterest {
    private final String categoryId;
    private final String categoryName;
    private final InterestLevel level;

    private CategoryInterest(String categoryId, String categoryName, InterestLevel level) {
        validateCategoryId(categoryId);
        validateCategoryName(categoryName);
        
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.level = Objects.requireNonNull(level, "관심도 레벨은 필수값입니다.");
    }

    public static CategoryInterest of(String categoryId, String categoryName, InterestLevel level) {
        return new CategoryInterest(categoryId, categoryName, level);
    }

    private void validateCategoryId(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리 ID는 필수값입니다.");
        }
    }

    private void validateCategoryName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리 이름은 필수값입니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryInterest that = (CategoryInterest) o;
        return Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId);
    }

    @Override
    public String toString() {
        return "CategoryInterest{" +
                "categoryId='" + categoryId + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", level=" + level +
                '}';
    }
}