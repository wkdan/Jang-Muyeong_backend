package com.jangmuyeong.remittance.infra.persistence.entity;

import static lombok.AccessLevel.*;

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
 * 계좌 영속성 엔티티
 */
@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
@Table(name = "accounts", uniqueConstraints = @UniqueConstraint(name = "uk_account_no", columnNames = "account_no"))
public class AccountJpaEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "account_no", nullable = false, length = 40)
	private String accountNo;

	@Column(nullable = false, length = 20)
	private String status; // ACTIVE/DELETED

	@Column(nullable = false)
	private long balance;

	public AccountJpaEntity(String accountNo, String status, long balance) {
		this.accountNo = accountNo;
		this.status = status;
		this.balance = balance;
	}

	/**
	 * 변경 감지 기반 업데이트, 포트 어댑터에서 도메인 객체의 상태를 반영할 때 사용
	 */
	public void update(String status, long balance) {
		this.status = status;
		this.balance = balance;
	}
}