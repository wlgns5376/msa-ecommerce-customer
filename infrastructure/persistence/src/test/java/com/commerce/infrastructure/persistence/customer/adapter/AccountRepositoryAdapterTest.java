package com.commerce.infrastructure.persistence.customer.adapter;

import com.commerce.customer.core.domain.model.*;
import com.commerce.infrastructure.persistence.customer.entity.AccountEntity;
import com.commerce.infrastructure.persistence.customer.mapper.AccountMapper;
import com.commerce.infrastructure.persistence.customer.repository.AccountJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountRepositoryAdapter 테스트")
class AccountRepositoryAdapterTest {

    @Mock
    private AccountJpaRepository accountJpaRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountRepositoryAdapter accountRepositoryAdapter;

    private Account testAccount;
    private AccountEntity testAccountEntity;
    private AccountId accountId;
    private CustomerId customerId;
    private Email email;

    @BeforeEach
    void setUp() {
        accountId = AccountId.of(1L);
        customerId = CustomerId.of(1L);
        email = Email.of("test@example.com");

        // 테스트용 Account 생성 (restore 사용하여 ID 고정)
        testAccount = Account.restore(
                accountId,
                customerId,
                email,
                Password.of("ValidPass123!"),
                AccountStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        testAccountEntity = AccountEntity.builder()
                .customerId(1L)
                .email("test@example.com")
                .password("ValidPass123!")
                .status(AccountEntity.AccountStatus.ACTIVE)
                .build();
        
        // 리플렉션으로 accountId 설정
        try {
            java.lang.reflect.Field accountIdField = testAccountEntity.getClass().getDeclaredField("accountId");
            accountIdField.setAccessible(true);
            accountIdField.set(testAccountEntity, 1L);
        } catch (Exception e) {
            // 테스트 환경에서만 사용되므로 예외 무시
        }
    }

    @Test
    @DisplayName("계정을 성공적으로 저장한다")
    void save_Success() {
        // Given
        given(accountMapper.toEntity(testAccount)).willReturn(testAccountEntity);
        given(accountJpaRepository.save(testAccountEntity)).willReturn(testAccountEntity);
        given(accountMapper.toDomain(testAccountEntity)).willReturn(testAccount);

        // When
        Account result = accountRepositoryAdapter.save(testAccount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        
        then(accountMapper).should(times(1)).toEntity(testAccount);
        then(accountJpaRepository).should(times(1)).save(testAccountEntity);
        then(accountMapper).should(times(1)).toDomain(testAccountEntity);
    }

    @Test
    @DisplayName("ID로 계정을 성공적으로 조회한다")
    void findById_Success() {
        // Given
        given(accountJpaRepository.findById(1L)).willReturn(Optional.of(testAccountEntity));
        given(accountMapper.toDomain(testAccountEntity)).willReturn(testAccount);

        // When
        Optional<Account> result = accountRepositoryAdapter.findById(accountId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAccountId()).isEqualTo(accountId);
        assertThat(result.get().getStatus()).isEqualTo(AccountStatus.ACTIVE);
        
        then(accountJpaRepository).should(times(1)).findById(1L);
        then(accountMapper).should(times(1)).toDomain(testAccountEntity);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 계정을 조회하면 빈 Optional을 반환한다")
    void findById_NotFound() {
        // Given
        given(accountJpaRepository.findById(1L)).willReturn(Optional.empty());

        // When
        Optional<Account> result = accountRepositoryAdapter.findById(accountId);

        // Then
        assertThat(result).isEmpty();
        
        then(accountJpaRepository).should(times(1)).findById(1L);
        then(accountMapper).should(times(0)).toDomain(any());
    }

    @Test
    @DisplayName("이메일로 계정을 성공적으로 조회한다")
    void findByEmail_Success() {
        // Given
        given(accountJpaRepository.findByEmail("test@example.com")).willReturn(Optional.of(testAccountEntity));
        given(accountMapper.toDomain(testAccountEntity)).willReturn(testAccount);

        // When
        Optional<Account> result = accountRepositoryAdapter.findByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        
        then(accountJpaRepository).should(times(1)).findByEmail("test@example.com");
        then(accountMapper).should(times(1)).toDomain(testAccountEntity);
    }

    @Test
    @DisplayName("고객 ID로 계정을 성공적으로 조회한다")
    void findByCustomerId_Success() {
        // Given
        given(accountJpaRepository.findByCustomerId(1L)).willReturn(Optional.of(testAccountEntity));
        given(accountMapper.toDomain(testAccountEntity)).willReturn(testAccount);

        // When
        Optional<Account> result = accountRepositoryAdapter.findByCustomerId(customerId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCustomerId()).isEqualTo(customerId);
        
        then(accountJpaRepository).should(times(1)).findByCustomerId(1L);
        then(accountMapper).should(times(1)).toDomain(testAccountEntity);
    }

    @Test
    @DisplayName("이메일 존재 여부를 확인한다")
    void existsByEmail_Success() {
        // Given
        given(accountJpaRepository.existsByEmail("test@example.com")).willReturn(true);

        // When
        boolean result = accountRepositoryAdapter.existsByEmail(email);

        // Then
        assertThat(result).isTrue();
        
        then(accountJpaRepository).should(times(1)).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("고객 ID 존재 여부를 확인한다")
    void existsByCustomerId_Success() {
        // Given
        given(accountJpaRepository.existsByCustomerId(1L)).willReturn(true);

        // When
        boolean result = accountRepositoryAdapter.existsByCustomerId(customerId);

        // Then
        assertThat(result).isTrue();
        
        then(accountJpaRepository).should(times(1)).existsByCustomerId(1L);
    }

    @Test
    @DisplayName("활성 계정을 이메일로 조회한다")
    void findActiveByEmail_Success() {
        // Given
        given(accountJpaRepository.findActiveAccountByEmail("test@example.com"))
                .willReturn(Optional.of(testAccountEntity));
        given(accountMapper.toDomain(testAccountEntity)).willReturn(testAccount);

        // When
        Optional<Account> result = accountRepositoryAdapter.findActiveByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(AccountStatus.ACTIVE);
        
        then(accountJpaRepository).should(times(1)).findActiveAccountByEmail("test@example.com");
        then(accountMapper).should(times(1)).toDomain(testAccountEntity);
    }

    @Test
    @DisplayName("활성 계정을 고객 ID로 조회한다")
    void findActiveByCustomerId_Success() {
        // Given
        given(accountJpaRepository.findActiveAccountByCustomerId(1L))
                .willReturn(Optional.of(testAccountEntity));
        given(accountMapper.toDomain(testAccountEntity)).willReturn(testAccount);

        // When
        Optional<Account> result = accountRepositoryAdapter.findActiveByCustomerId(customerId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(AccountStatus.ACTIVE);
        
        then(accountJpaRepository).should(times(1)).findActiveAccountByCustomerId(1L);
        then(accountMapper).should(times(1)).toDomain(testAccountEntity);
    }

    @Test
    @DisplayName("계정을 성공적으로 삭제한다")
    void delete_Success() {
        // Given
        given(accountJpaRepository.findById(1L)).willReturn(Optional.of(testAccountEntity));
        given(accountJpaRepository.save(testAccountEntity)).willReturn(testAccountEntity);

        // When
        accountRepositoryAdapter.delete(testAccount);

        // Then
        then(accountJpaRepository).should(times(1)).findById(1L);
        then(accountJpaRepository).should(times(1)).save(testAccountEntity);
        // 물리적 삭제 대신 soft delete 확인
        then(accountJpaRepository).should(never()).deleteById(anyLong());
    }
}