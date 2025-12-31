package com.jangmuyeong.remittance.infra.persistence.adapter;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.jangmuyeong.remittance.domain.ledger.LedgerEntry;
import com.jangmuyeong.remittance.domain.port.LedgerPort;
import com.jangmuyeong.remittance.infra.persistence.mapper.LedgerMapper;
import com.jangmuyeong.remittance.infra.persistence.repository.LedgerEntryJpaRepository;

/**
 * LedgerPort의 JPA 구현체
 */
@Component
public class LedgerPortAdapter implements LedgerPort {

	private final LedgerEntryJpaRepository repo;

	public LedgerPortAdapter(LedgerEntryJpaRepository repo) {
		this.repo = repo;
	}

	@Override
	public LedgerEntry save(LedgerEntry entry) {
		return LedgerMapper.toDomain(repo.save(LedgerMapper.toNewEntity(entry)));
	}

	@Override
	public List<LedgerEntry> findLatestByAccountId(Long accountId, int size) {
		return repo.findByAccountIdOrderByOccurredAtDescIdDesc(accountId, PageRequest.of(0, size))
			.stream()
			.map(LedgerMapper::toDomain)
			.toList();
	}
}