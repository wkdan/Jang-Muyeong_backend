package com.jangmuyeong.remittance.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.jangmuyeong.remittance.domain.account.Account;
import com.jangmuyeong.remittance.domain.account.AccountStatus;
import com.jangmuyeong.remittance.domain.exception.DomainException;
import com.jangmuyeong.remittance.domain.ledger.LedgerEntry;
import com.jangmuyeong.remittance.domain.port.AccountPort;
import com.jangmuyeong.remittance.domain.port.LedgerPort;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TransactionQueryServiceTest {

	@Mock AccountPort accountPort;
	@Mock LedgerPort ledgerPort;

	@InjectMocks TransactionQueryService service;

	@Test
	void latest_returns_ledger_list_when_account_exists() {
		when(accountPort.findById(1L))
			.thenReturn(Optional.of(new Account(1L, "111-222", AccountStatus.ACTIVE, 0L)));

		List<LedgerEntry> ledgers = List.of(
			new LedgerEntry(1L, 1L, null, com.jangmuyeong.remittance.domain.ledger.TransactionType.DEPOSIT, 1000, 0, Instant.now())
		);
		when(ledgerPort.findLatestByAccountId(1L, 20)).thenReturn(ledgers);

		var res = service.latest(1L, 20);

		assertThat(res).hasSize(1);
		verify(ledgerPort).findLatestByAccountId(1L, 20);
	}

	@Test
	void latest_throws_when_account_not_found() {
		when(accountPort.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.latest(999L, 20))
			.isInstanceOf(DomainException.class);

		verify(ledgerPort, never()).findLatestByAccountId(anyLong(), anyInt());
	}
}
