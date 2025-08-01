package com.commerce.infrastructure.persistence.customer.repository;

import com.commerce.infrastructure.persistence.config.TestJpaConfig;
import com.commerce.infrastructure.persistence.customer.entity.AccountEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestJpaConfig.class)
class AccountQueryRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EntityManager entityManager;

    private AccountQueryRepository accountQueryRepository;

    @BeforeEach
    void setUp() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        accountQueryRepository = new AccountQueryRepository(queryFactory);
    }

    @Test
    @DisplayName("이메일로 활성 계정을 조회한다 - 삭제되지 않은 계정만")
    void findByEmail_ShouldReturnAccount_WhenNotDeleted() {
        // given
        String email = "test@example.com";
        AccountEntity account = createAccount(email, 1001L, AccountEntity.AccountStatus.ACTIVE, false);
        em.persistAndFlush(account);

        // when
        Optional<AccountEntity> result = accountQueryRepository.findByEmail(email);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        assertThat(result.get().isDeleted()).isFalse();
    }

    @Test
    @DisplayName("이메일로 조회 시 삭제된 계정은 반환하지 않는다")
    void findByEmail_ShouldReturnEmpty_WhenDeleted() {
        // given
        String email = "deleted@example.com";
        AccountEntity account = createAccount(email, 1002L, AccountEntity.AccountStatus.ACTIVE, true);
        em.persistAndFlush(account);

        // when
        Optional<AccountEntity> result = accountQueryRepository.findByEmail(email);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("고객 ID로 활성 계정을 조회한다 - 삭제되지 않은 계정만")
    void findByCustomerId_ShouldReturnAccount_WhenNotDeleted() {
        // given
        Long customerId = 2001L;
        AccountEntity account = createAccount("customer@example.com", customerId, AccountEntity.AccountStatus.ACTIVE, false);
        em.persistAndFlush(account);

        // when
        Optional<AccountEntity> result = accountQueryRepository.findByCustomerId(customerId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCustomerId()).isEqualTo(customerId);
        assertThat(result.get().isDeleted()).isFalse();
    }

    @Test
    @DisplayName("고객 ID로 조회 시 삭제된 계정은 반환하지 않는다")
    void findByCustomerId_ShouldReturnEmpty_WhenDeleted() {
        // given
        Long customerId = 2002L;
        AccountEntity account = createAccount("deleted@example.com", customerId, AccountEntity.AccountStatus.ACTIVE, true);
        em.persistAndFlush(account);

        // when
        Optional<AccountEntity> result = accountQueryRepository.findByCustomerId(customerId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이메일 존재 여부를 확인한다 - 삭제되지 않은 계정만")
    void existsByEmail_ShouldReturnTrue_WhenNotDeleted() {
        // given
        String email = "exists@example.com";
        AccountEntity account = createAccount(email, 3001L, AccountEntity.AccountStatus.ACTIVE, false);
        em.persistAndFlush(account);

        // when
        boolean exists = accountQueryRepository.existsByEmail(email);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("삭제된 계정의 이메일은 존재하지 않는 것으로 처리한다")
    void existsByEmail_ShouldReturnFalse_WhenDeleted() {
        // given
        String email = "deleted@example.com";
        AccountEntity account = createAccount(email, 3002L, AccountEntity.AccountStatus.ACTIVE, true);
        em.persistAndFlush(account);

        // when
        boolean exists = accountQueryRepository.existsByEmail(email);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("고객 ID 존재 여부를 확인한다 - 삭제되지 않은 계정만")
    void existsByCustomerId_ShouldReturnTrue_WhenNotDeleted() {
        // given
        Long customerId = 4001L;
        AccountEntity account = createAccount("exists@example.com", customerId, AccountEntity.AccountStatus.ACTIVE, false);
        em.persistAndFlush(account);

        // when
        boolean exists = accountQueryRepository.existsByCustomerId(customerId);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("삭제된 계정의 고객 ID는 존재하지 않는 것으로 처리한다")
    void existsByCustomerId_ShouldReturnFalse_WhenDeleted() {
        // given
        Long customerId = 4002L;
        AccountEntity account = createAccount("deleted@example.com", customerId, AccountEntity.AccountStatus.ACTIVE, true);
        em.persistAndFlush(account);

        // when
        boolean exists = accountQueryRepository.existsByCustomerId(customerId);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("이메일로 ACTIVE 상태의 계정을 조회한다")
    void findActiveAccountByEmail_ShouldReturnAccount_WhenActiveAndNotDeleted() {
        // given
        String email = "active@example.com";
        AccountEntity activeAccount = createAccount(email, 5001L, AccountEntity.AccountStatus.ACTIVE, false);
        em.persistAndFlush(activeAccount);

        // when
        Optional<AccountEntity> result = accountQueryRepository.findActiveAccountByEmail(email);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        assertThat(result.get().getStatus()).isEqualTo(AccountEntity.AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("INACTIVE 상태의 계정은 활성 계정 조회에서 제외된다")
    void findActiveAccountByEmail_ShouldReturnEmpty_WhenInactive() {
        // given
        String email = "inactive@example.com";
        AccountEntity inactiveAccount = createAccount(email, 5002L, AccountEntity.AccountStatus.INACTIVE, false);
        em.persistAndFlush(inactiveAccount);

        // when
        Optional<AccountEntity> result = accountQueryRepository.findActiveAccountByEmail(email);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SUSPENDED 상태의 계정은 활성 계정 조회에서 제외된다")
    void findActiveAccountByEmail_ShouldReturnEmpty_WhenSuspended() {
        // given
        String email = "suspended@example.com";
        AccountEntity suspendedAccount = createAccount(email, 5003L, AccountEntity.AccountStatus.SUSPENDED, false);
        em.persistAndFlush(suspendedAccount);

        // when
        Optional<AccountEntity> result = accountQueryRepository.findActiveAccountByEmail(email);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("고객 ID로 ACTIVE 상태의 계정을 조회한다")
    void findActiveAccountByCustomerId_ShouldReturnAccount_WhenActiveAndNotDeleted() {
        // given
        Long customerId = 6001L;
        AccountEntity activeAccount = createAccount("active@example.com", customerId, AccountEntity.AccountStatus.ACTIVE, false);
        em.persistAndFlush(activeAccount);

        // when
        Optional<AccountEntity> result = accountQueryRepository.findActiveAccountByCustomerId(customerId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCustomerId()).isEqualTo(customerId);
        assertThat(result.get().getStatus()).isEqualTo(AccountEntity.AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("고객 ID로 조회 시 INACTIVE 상태의 계정은 활성 계정 조회에서 제외된다")
    void findActiveAccountByCustomerId_ShouldReturnEmpty_WhenInactive() {
        // given
        Long customerId = 6002L;
        AccountEntity inactiveAccount = createAccount("inactive@example.com", customerId, AccountEntity.AccountStatus.INACTIVE, false);
        em.persistAndFlush(inactiveAccount);

        // when
        Optional<AccountEntity> result = accountQueryRepository.findActiveAccountByCustomerId(customerId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("ID로 활성 계정을 조회한다 - 삭제되지 않은 계정만")
    void findById_ShouldReturnAccount_WhenNotDeleted() {
        // given
        AccountEntity account = createAccount("byid@example.com", 7001L, AccountEntity.AccountStatus.ACTIVE, false);
        AccountEntity savedAccount = em.persistAndFlush(account);
        Long accountId = savedAccount.getAccountId();

        // when
        Optional<AccountEntity> result = accountQueryRepository.findById(accountId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getAccountId()).isEqualTo(accountId);
        assertThat(result.get().isDeleted()).isFalse();
    }

    @Test
    @DisplayName("ID로 조회 시 삭제된 계정은 반환하지 않는다")
    void findById_ShouldReturnEmpty_WhenDeleted() {
        // given
        AccountEntity account = createAccount("deleted@example.com", 7002L, AccountEntity.AccountStatus.ACTIVE, true);
        AccountEntity savedAccount = em.persistAndFlush(account);
        Long accountId = savedAccount.getAccountId();

        // when
        Optional<AccountEntity> result = accountQueryRepository.findById(accountId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 빈 Optional을 반환한다")
    void findByEmail_ShouldReturnEmpty_WhenNotExists() {
        // when
        Optional<AccountEntity> result = accountQueryRepository.findByEmail("notexists@example.com");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 고객 ID로 조회 시 빈 Optional을 반환한다")
    void findByCustomerId_ShouldReturnEmpty_WhenNotExists() {
        // when
        Optional<AccountEntity> result = accountQueryRepository.findByCustomerId(99999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("여러 계정 중 특정 이메일의 계정만 정확히 조회한다")
    void findByEmail_ShouldReturnCorrectAccount_WhenMultipleAccountsExist() {
        // given
        AccountEntity account1 = createAccount("first@example.com", 8001L, AccountEntity.AccountStatus.ACTIVE, false);
        AccountEntity account2 = createAccount("second@example.com", 8002L, AccountEntity.AccountStatus.ACTIVE, false);
        AccountEntity account3 = createAccount("third@example.com", 8003L, AccountEntity.AccountStatus.INACTIVE, false);
        
        em.persistAndFlush(account1);
        em.persistAndFlush(account2);
        em.persistAndFlush(account3);

        // when
        Optional<AccountEntity> result = accountQueryRepository.findByEmail("second@example.com");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("second@example.com");
        assertThat(result.get().getCustomerId()).isEqualTo(8002L);
    }

    private AccountEntity createAccount(String email, Long customerId, AccountEntity.AccountStatus status, boolean deleted) {
        AccountEntity account = AccountEntity.builder()
                .email(email)
                .customerId(customerId)
                .password("encodedPassword")
                .status(status)
                .build();
        
        if (deleted) {
            account.markAsDeleted();
        }
        
        return account;
    }
}