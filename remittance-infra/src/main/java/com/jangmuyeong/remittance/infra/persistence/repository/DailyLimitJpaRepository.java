package com.jangmuyeong.remittance.infra.persistence.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.jangmuyeong.remittance.infra.persistence.entity.DailyLimitJpaEntity;

import jakarta.persistence.LockModeType;

/**
 * DailyLimitJpaEntity 조회/저장용 레포지토리
 * 한도 누적치는 "하루 기준"으로 업데이트되므로,
 * getOrCreate 과정에서 동시 생성/업데이트 경쟁 상태가 발생할 수 있어
 * for-update 조회 메서드를 제공한다.
 */
public interface DailyLimitJpaRepository extends JpaRepository<DailyLimitJpaEntity, Long> {

	Optional<DailyLimitJpaEntity> findByAccountIdAndDate(Long accountId, LocalDate date);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select d from DailyLimitJpaEntity d where d.accountId = :accountId and d.date = :date")
	Optional<DailyLimitJpaEntity> findByAccountIdAndDateForUpdate(Long accountId, LocalDate date);
}