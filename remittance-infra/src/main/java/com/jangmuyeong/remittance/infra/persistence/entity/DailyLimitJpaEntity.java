package com.jangmuyeong.remittance.infra.persistence.entity;

import static lombok.AccessLevel.*;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 일 한도 영속성 엔티티
 * 출금/이체 누적ㅎ바은 도메인에서 검증하고, 여기서는 저장만 담당
 */
@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
@Table(name = "daily_limits",
	uniqueConstraints = @UniqueConstraint(name = "uk_daily_limit", columnNames = {"account_id", "limit_date"}))
public class DailyLimitJpaEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "account_id", nullable = false)
	private Long accountId;

	@Column(name = "limit_date", nullable = false)
	private LocalDate date;

	@Column(nullable = false)
	private long withdrawSum;

	@Column(nullable = false)
	private long transferSum;

	public DailyLimitJpaEntity(Long accountId, LocalDate date, long withdrawSum, long transferSum) {
		this.accountId = accountId;
		this.date = date;
		this.withdrawSum = withdrawSum;
		this.transferSum = transferSum;
	}

	public void update(long withdrawSum, long transferSum) {
		this.withdrawSum = withdrawSum;
		this.transferSum = transferSum;
	}
}