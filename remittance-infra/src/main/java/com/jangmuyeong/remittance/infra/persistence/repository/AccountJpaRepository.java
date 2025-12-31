package com.jangmuyeong.remittance.infra.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.jangmuyeong.remittance.infra.persistence.entity.AccountJpaEntity;

import jakarta.persistence.LockModeType;

/**
 * AccountJpaEntity를 위한 레포지토리
 * 단순 조회는 기본 메서드를 사용하고, 경쟁 상태가 생기는 작업은 비관락을 잡은 뒤 처리
 */
public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, Long> {

	Optional<AccountJpaEntity> findByAccountNo(String accountNo);

	// 동시 출금/이체 시 정합성 보장을 위해 비관락 사용
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from AccountJpaEntity a where a.id = :id")
	Optional<AccountJpaEntity> findByIdForUpdate(Long id);
}