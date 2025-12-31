package com.jangmuyeong.remittance.infra.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.jangmuyeong.remittance.domain.account.Account;
import com.jangmuyeong.remittance.domain.port.AccountPort;
import com.jangmuyeong.remittance.infra.persistence.entity.AccountJpaEntity;
import com.jangmuyeong.remittance.infra.persistence.mapper.AccountMapper;
import com.jangmuyeong.remittance.infra.persistence.repository.AccountJpaRepository;

/**
 * AccountPort의 JPA 구현체
 * 애플리케이션/도메인은 AccountPort 인터페이스만 의존
 * 이 어댑터에서 JPA Repository를 호출하고, 도메인 ↔ 엔티티 변환을 수행
 */
@Component
public class AccountPortAdapter implements AccountPort {

	private final AccountJpaRepository repo;

	public AccountPortAdapter(AccountJpaRepository repo) {
		this.repo = repo;
	}

	@Override
	public Optional<Account> findById(Long accountId) {
		return repo.findById(accountId).map(AccountMapper::toDomain);
	}

	@Override
	public Optional<Account> findByIdForUpdate(Long accountId) {
		// 잔액 변경(입금/출금/이체)은 비관락으로 조회
		return repo.findByIdForUpdate(accountId).map(AccountMapper::toDomain);
	}

	@Override
	public Optional<Account> findByAccountNo(String accountNo) {
		return repo.findByAccountNo(accountNo).map(AccountMapper::toDomain);
	}

	@Override
	public Account save(Account account) {
		// 신규 생성: INSERT
		if (account.getId() == null) {
			AccountJpaEntity saved = repo.save(AccountMapper.toNewEntity(account));
			return AccountMapper.toDomain(saved);
		}
		// 수정: 영속 상태 엔티티를 가져와 dirty-checking으로 UPDATE
		// (존재하지 않으면 NoSuchElementException -> 상위 계층에서 NOT_FOUND로 처리)
		AccountJpaEntity entity = repo.findById(account.getId()).orElseThrow();
		AccountMapper.apply(account, entity);
		return AccountMapper.toDomain(entity);
	}
}