package com.commerce.infrastructure.persistence.customer.repository;

import com.commerce.infrastructure.persistence.customer.entity.CustomerProfileEntity;
import com.commerce.infrastructure.persistence.customer.entity.BrandPreferenceEntity;
import com.commerce.infrastructure.persistence.customer.entity.CategoryInterestEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.commerce.infrastructure.persistence.customer.entity.QCustomerProfileEntity.customerProfileEntity;
import static com.commerce.infrastructure.persistence.customer.entity.QAddressEntity.addressEntity;
import static com.commerce.infrastructure.persistence.customer.entity.QBrandPreferenceEntity.brandPreferenceEntity;
import static com.commerce.infrastructure.persistence.customer.entity.QCategoryInterestEntity.categoryInterestEntity;

/**
 * CustomerProfile을 위한 QueryDSL Repository
 * 복잡한 조인 쿼리, 동적 쿼리, 성능 최적화를 담당
 */
@Repository
@RequiredArgsConstructor
public class CustomerProfileQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 모든 연관 데이터와 함께 CustomerProfile 조회 (N+1 문제 해결)
     */
    public Optional<CustomerProfileEntity> findWithAllDetailsById(Long customerId) {
        // MultipleBagFetchException을 피하기 위해 한 번에 하나의 컬렉션만 fetch join
        CustomerProfileEntity profile = queryFactory
                .selectFrom(customerProfileEntity)
                .leftJoin(customerProfileEntity.addresses, addressEntity).fetchJoin()
                .where(customerProfileEntity.customerId.eq(customerId))
                .fetchOne();

        if (profile != null) {
            // Hibernate.initialize를 사용하여 나머지 컬렉션 초기화
            Hibernate.initialize(profile.getBrandPreferences());
            Hibernate.initialize(profile.getCategoryInterests());
        }

        return Optional.ofNullable(profile);
    }

    /**
     * 동적 검색 조건으로 CustomerProfile 목록 조회
     */
    public Page<CustomerProfileEntity> findBySearchConditions(
            String firstName, String lastName,
            LocalDate birthDateFrom, LocalDate birthDateTo,
            CustomerProfileEntity.Gender gender,
            CustomerProfileEntity.ProfileStatus status,
            Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();

        // 동적 조건 추가
        if (firstName != null && !firstName.trim().isEmpty()) {
            builder.and(customerProfileEntity.firstName.containsIgnoreCase(firstName));
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            builder.and(customerProfileEntity.lastName.containsIgnoreCase(lastName));
        }
        if (birthDateFrom != null) {
            builder.and(customerProfileEntity.birthDate.goe(birthDateFrom));
        }
        if (birthDateTo != null) {
            builder.and(customerProfileEntity.birthDate.loe(birthDateTo));
        }
        if (gender != null) {
            builder.and(customerProfileEntity.gender.eq(gender));
        }
        if (status != null) {
            builder.and(customerProfileEntity.status.eq(status));
        }

        // 결과 조회
        List<CustomerProfileEntity> results = queryFactory
                .selectFrom(customerProfileEntity)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(customerProfileEntity.createdAt.desc())
                .fetch();

        // 총 개수 조회
        Long total = queryFactory
                .select(customerProfileEntity.count())
                .from(customerProfileEntity)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(results, pageable, total != null ? total : 0);
    }

    /**
     * 특정 브랜드를 선호하는 고객 목록 조회
     */
    public List<CustomerProfileEntity> findByPreferredBrand(String brandName, int limit) {
        // H2 데이터베이스 호환성을 위해 enum 값을 직접 정렬하지 않고
        // 결과를 가져온 후 Java에서 정렬
        List<CustomerProfileEntity> results = queryFactory
                .selectFrom(customerProfileEntity)
                .distinct()
                .join(customerProfileEntity.brandPreferences, brandPreferenceEntity).fetchJoin()
                .where(brandPreferenceEntity.brandName.eq(brandName))
                .limit(limit * 2) // 정렬 전에 여유있게 가져옴
                .fetch();
        
        // Java에서 선호도 순으로 정렬
        return results.stream()
                .sorted((p1, p2) -> {
                    BrandPreferenceEntity pref1 = p1.getBrandPreferences().stream()
                            .filter(bp -> bp.getBrandName().equals(brandName))
                            .findFirst().orElse(null);
                    BrandPreferenceEntity pref2 = p2.getBrandPreferences().stream()
                            .filter(bp -> bp.getBrandName().equals(brandName))
                            .findFirst().orElse(null);
                    
                    if (pref1 == null || pref2 == null) return 0;
                    
                    // LOVE(0) > LIKE(1) > DISLIKE(2) 순으로 정렬
                    return pref1.getPreferenceLevel().ordinal() - pref2.getPreferenceLevel().ordinal();
                })
                .limit(limit)
                .toList();
    }

    /**
     * 특정 카테고리에 관심있는 고객 목록 조회
     */
    public List<CustomerProfileEntity> findByInterestCategory(String categoryName, int limit) {
        // H2 데이터베이스 호환성을 위해 enum 값을 직접 정렬하지 않고
        // 결과를 가져온 후 Java에서 정렬
        List<CustomerProfileEntity> results = queryFactory
                .selectFrom(customerProfileEntity)
                .distinct()
                .join(customerProfileEntity.categoryInterests, categoryInterestEntity).fetchJoin()
                .where(categoryInterestEntity.categoryName.eq(categoryName))
                .limit(limit * 2) // 정렬 전에 여유있게 가져옴
                .fetch();
        
        // Java에서 관심도 순으로 정렬
        return results.stream()
                .sorted((p1, p2) -> {
                    CategoryInterestEntity interest1 = p1.getCategoryInterests().stream()
                            .filter(ci -> ci.getCategoryName().equals(categoryName))
                            .findFirst().orElse(null);
                    CategoryInterestEntity interest2 = p2.getCategoryInterests().stream()
                            .filter(ci -> ci.getCategoryName().equals(categoryName))
                            .findFirst().orElse(null);
                    
                    if (interest1 == null || interest2 == null) return 0;
                    
                    // HIGH(0) > MEDIUM(1) > LOW(2) 순으로 정렬
                    return interest1.getInterestLevel().ordinal() - interest2.getInterestLevel().ordinal();
                })
                .limit(limit)
                .toList();
    }

    /**
     * 최근 활성 고객 조회 (특정 기간 내 프로필 업데이트)
     */
    public List<CustomerProfileEntity> findRecentlyActiveCustomers(LocalDateTime since, int limit) {
        return queryFactory
                .selectFrom(customerProfileEntity)
                .where(customerProfileEntity.updatedAt.goe(since)
                        .and(customerProfileEntity.status.eq(CustomerProfileEntity.ProfileStatus.ACTIVE)))
                .orderBy(customerProfileEntity.updatedAt.desc())
                .limit(limit)
                .fetch();
    }

    /**
     * 생일이 특정 기간 내인 고객 조회 (마케팅용)
     */
    public List<CustomerProfileEntity> findCustomersWithBirthdayInRange(
            LocalDate startDate, LocalDate endDate) {
        return queryFactory
                .selectFrom(customerProfileEntity)
                .where(customerProfileEntity.birthDate.between(startDate, endDate)
                        .and(customerProfileEntity.status.eq(CustomerProfileEntity.ProfileStatus.ACTIVE)))
                .orderBy(customerProfileEntity.birthDate.asc())
                .fetch();
    }

    /**
     * 통계: 성별별 고객 수
     */
    public List<Object[]> getCustomerCountByGender() {
        return queryFactory
                .select(customerProfileEntity.gender, customerProfileEntity.count())
                .from(customerProfileEntity)
                .where(customerProfileEntity.status.eq(CustomerProfileEntity.ProfileStatus.ACTIVE))
                .groupBy(customerProfileEntity.gender)
                .fetch()
                .stream()
                .map(tuple -> new Object[]{tuple.get(0, CustomerProfileEntity.Gender.class), tuple.get(1, Long.class)})
                .toList();
    }
}