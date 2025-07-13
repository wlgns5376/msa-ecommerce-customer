package com.commerce.infrastructure.persistence.customer.entity;

import com.commerce.infrastructure.persistence.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category_interests", 
       indexes = {
           @Index(name = "idx_category_interest_profile_id", columnList = "profile_id"),
           @Index(name = "idx_category_interest_category_name", columnList = "category_name")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_profile_category", columnNames = {"profile_id", "category_name"})
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryInterestEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_interest_id")
    private Long categoryInterestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private CustomerProfileEntity customerProfile;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_level", nullable = false, length = 20)
    private InterestLevel interestLevel;

    @Builder
    public CategoryInterestEntity(CustomerProfileEntity customerProfile, String categoryName, 
                                InterestLevel interestLevel) {
        this.customerProfile = customerProfile;
        this.categoryName = categoryName;
        this.interestLevel = interestLevel;
    }

    public void updateInterestLevel(InterestLevel interestLevel) {
        this.interestLevel = interestLevel;
    }

    // JPA를 위한 setter (package-private)
    void setCustomerProfile(CustomerProfileEntity customerProfile) {
        this.customerProfile = customerProfile;
    }

    public enum InterestLevel {
        HIGH, MEDIUM, LOW
    }
}