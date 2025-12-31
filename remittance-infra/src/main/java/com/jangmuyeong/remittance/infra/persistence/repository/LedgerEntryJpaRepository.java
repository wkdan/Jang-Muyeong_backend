package com.jangmuyeong.remittance.infra.persistence.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jangmuyeong.remittance.infra.persistence.entity.LedgerEntryJpaEntity;

/**
 * 거래 기록 조회용 레포지토리
 * 거래내역 조회는 최신순 정렬이 필요하므로 내림차순 페이징 조회
 */
public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryJpaEntity, Long> {
	List<LedgerEntryJpaEntity> findByAccountIdOrderByOccurredAtDescIdDesc(Long accountId, Pageable pageable);
}