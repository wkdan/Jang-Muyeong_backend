package com.jangmuyeong.remittance.infra.persistence.mapper;

import com.jangmuyeong.remittance.domain.limit.DailyLimit;
import com.jangmuyeong.remittance.infra.persistence.entity.DailyLimitJpaEntity;

public class DailyLimitMapper {

	/** JPA 엔티티 → 도메인 */
	public static DailyLimit toDomain(DailyLimitJpaEntity e) {
		return new DailyLimit(e.getId(), e.getAccountId(), e.getDate(), e.getWithdrawSum(), e.getTransferSum());
	}

	/** 도메인 → 신규 엔티티(Insert) */
	public static DailyLimitJpaEntity toNewEntity(DailyLimit d) {
		return new DailyLimitJpaEntity(d.getAccountId(), d.getDate(), d.getWithdrawSum(), d.getTransferSum());
	}

	/** 도메인 상태를 영속성 엔티티에 반영(Dirty Checking) */
	public static void apply(DailyLimit d, DailyLimitJpaEntity e) {
		e.update(d.getWithdrawSum(), d.getTransferSum());
	}
}