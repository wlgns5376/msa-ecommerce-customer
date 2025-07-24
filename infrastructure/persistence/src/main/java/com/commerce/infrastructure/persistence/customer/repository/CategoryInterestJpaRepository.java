package com.commerce.infrastructure.persistence.customer.repository;

import com.commerce.infrastructure.persistence.customer.entity.CategoryInterestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryInterestJpaRepository extends JpaRepository<CategoryInterestEntity, Long> {
}