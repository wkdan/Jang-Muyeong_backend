package com.jangmuyeong.remittance.infra.persistence.mapper;

import com.jangmuyeong.remittance.domain.account.Account;
import com.jangmuyeong.remittance.domain.account.AccountStatus;
import com.jangmuyeong.remittance.infra.persistence.entity.AccountJpaEntity;

public class AccountMapper {

	public static Account toDomain(AccountJpaEntity e) {
		// Entity의 status는 문자열로 저장하므로 도메인 enum으로 복원
		return new Account(e.getId(), e.getAccountNo(), AccountStatus.valueOf(e.getStatus()), e.getBalance());
	}

	public static AccountJpaEntity toNewEntity(Account a) {
		// 신규 저장용 엔티티 생성 (id는 DB에서 생성)
		return new AccountJpaEntity(a.getAccountNo(), a.getStatus().name(), a.getBalance());
	}

	public static void apply(Account a, AccountJpaEntity e) {
		// 영속성 엔티티를 도메인 상태로 동기화 (dirty checking 대상)
		e.update(a.getStatus().name(), a.getBalance());
	}
}