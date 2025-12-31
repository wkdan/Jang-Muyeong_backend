package com.jangmuyeong.remittance.infra.persistence.adapter;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.jangmuyeong.remittance.domain.limit.DailyLimit;
import com.jangmuyeong.remittance.domain.port.DailyLimitPort;
import com.jangmuyeong.remittance.infra.persistence.entity.DailyLimitJpaEntity;
import com.jangmuyeong.remittance.infra.persistence.mapper.DailyLimitMapper;
import com.jangmuyeong.remittance.infra.persistence.repository.DailyLimitJpaRepository;

/**
 * DailyLimitPort의 JPA 구현체
 */
@Component
public class DailyLimitPortAdapter implements DailyLimitPort {

	private final DailyLimitJpaRepository repo;

	public DailyLimitPortAdapter(DailyLimitJpaRepository repo) {
		this.repo = repo;
	}

	@Override
	public DailyLimit getOrCreate(Long accountId, LocalDate date) {
		return repo.findByAccountIdAndDateForUpdate(accountId, date)
			.map(DailyLimitMapper::toDomain)
			.orElseGet(() -> {
				try {
					DailyLimitJpaEntity saved = repo.save(new DailyLimitJpaEntity(accountId, date, 0L, 0L));
					return DailyLimitMapper.toDomain(saved);
				} catch (org.springframework.dao.DataIntegrityViolationException ex) {
					// 동시에 생성된 경우: 다시 락 조회
					return repo.findByAccountIdAndDateForUpdate(accountId, date)
						.map(DailyLimitMapper::toDomain)
						.orElseThrow();
				}
			});
	}

	@Override
	public DailyLimit save(DailyLimit limit) {
		if (limit.getId() == null) {
			DailyLimitJpaEntity saved = repo.save(DailyLimitMapper.toNewEntity(limit));
			return DailyLimitMapper.toDomain(saved);
		}
		DailyLimitJpaEntity entity = repo.findById(limit.getId()).orElseThrow();
		DailyLimitMapper.apply(limit, entity);
		return DailyLimitMapper.toDomain(entity);
	}
}
