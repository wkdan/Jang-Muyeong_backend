package com.jangmuyeong.remittance.infra.persistence.mapper;

import com.jangmuyeong.remittance.domain.ledger.LedgerEntry;
import com.jangmuyeong.remittance.domain.ledger.TransactionType;
import com.jangmuyeong.remittance.infra.persistence.entity.LedgerEntryJpaEntity;

public class LedgerMapper {

	/** 신규 원장 기록은 항상 INSERT이므로, id 없는 엔티티로 생성 */
	public static LedgerEntryJpaEntity toNewEntity(LedgerEntry e) {
		return new LedgerEntryJpaEntity(
			e.getAccountId(),
			e.getCounterpartyAccountId(),
			e.getType().name(),
			e.getAmount(),
			e.getFeeAmount(),
			e.getOccurredAt(),
			e.getBalanceAfter()
		);
	}

	/** 조회 결과를 서비스/응용계층에서 쓰기 위해 도메인 모델로 복원 */
	public static LedgerEntry toDomain(LedgerEntryJpaEntity e) {
		return new LedgerEntry(
			e.getId(),
			e.getAccountId(),
			e.getCounterpartyAccountId(),
			TransactionType.valueOf(e.getType()),
			e.getAmount(),
			e.getFeeAmount(),
			e.getOccurredAt(),
			e.getBalanceAfter()
		);
	}
}