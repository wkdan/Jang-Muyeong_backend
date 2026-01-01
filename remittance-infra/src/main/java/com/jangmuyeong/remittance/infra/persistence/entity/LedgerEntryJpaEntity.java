package com.jangmuyeong.remittance.infra.persistence.entity;

import static lombok.AccessLevel.*;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 거래 기록 영속성 엔티티
 * 입금/출금/이체/수수료를 한 테이블로 기록
 * 조회 성능을 위해 (account_id, occurred_at) 인덱스 둠
 */
@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
@Table(name = "ledger_entries",
	indexes = @Index(name = "idx_ledger_account_time", columnList = "account_id, occurred_at"))
public class LedgerEntryJpaEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "account_id", nullable = false)
	private Long accountId;

	@Column(name = "counterparty_account_id")
	private Long counterpartyAccountId;

	@Column(nullable = false, length = 30)
	private String type;

	@Column(nullable = false)
	private long amount;

	@Column(name = "fee_amount", nullable = false)
	private long feeAmount;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Column(name = "balance_after", nullable = false)
	private long balanceAfter;

	public LedgerEntryJpaEntity(Long accountId, Long counterpartyAccountId, String type,
		long amount, long feeAmount, Instant occurredAt, long balanceAfter) {
		this.accountId = accountId;
		this.counterpartyAccountId = counterpartyAccountId;
		this.type = type;
		this.amount = amount;
		this.feeAmount = feeAmount;
		this.occurredAt = occurredAt;
		this.balanceAfter = balanceAfter;
	}
}