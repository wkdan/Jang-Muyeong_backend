package com.jangmuyeong.remittance;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RemittanceApiIntegrationTest {

	// Bean 주입 안 씀(IDE 경고 회피)
	private final ObjectMapper om = new ObjectMapper();
	@Autowired MockMvc mvc;

	// -------------------- 1) 이체 수수료 1% --------------------
	@Test
	void remit_applies_fee_1percent_and_updates_balances() throws Exception {
		long aId = createAccount(randomAccountNo("A"));
		long bId = createAccount(randomAccountNo("B"));

		deposit(aId, 1_000_000);

		JsonNode data = json(remit(aId, bId, 100_000, 200)).path("data");

		assertThat(data.path("fee").asLong()).isEqualTo(1_000);
		assertThat(data.path("fromBalance").asLong()).isEqualTo(899_000); // 1,000,000 - 100,000 - 1,000
		assertThat(data.path("toBalance").asLong()).isEqualTo(100_000);
	}

	// -------------------- 2) 출금 일한도 1,000,000 --------------------
	@Test
	void withdraw_daily_limit_1_000_000_is_enforced() throws Exception {
		long aId = createAccount(randomAccountNo("W"));
		deposit(aId, 2_000_000);

		withdraw(aId, 900_000, 200); // OK

		JsonNode err = json(withdraw(aId, 200_000, 400)); // 누적 1,100,000 -> FAIL
		assertThat(err.path("code").asText()).isEqualTo("WITHDRAW_DAILY_LIMIT_EXCEEDED");
	}

	// -------------------- 3) 이체 일한도 3,000,000 --------------------
	@Test
	void transfer_daily_limit_3_000_000_is_enforced() throws Exception {
		long fromId = createAccount(randomAccountNo("F"));
		long toId = createAccount(randomAccountNo("T"));

		deposit(fromId, 10_000_000);

		remit(fromId, toId, 2_000_000, 200); // OK

		JsonNode err = json(remit(fromId, toId, 1_500_000, 400)); // 누적 3,500,000 -> FAIL
		assertThat(err.path("code").asText()).isEqualTo("TRANSFER_DAILY_LIMIT_EXCEEDED");
	}

	// -------------------- 4) 거래내역 최신순 --------------------
	@Test
	void transactions_are_returned_in_latest_order() throws Exception {
		long aId = createAccount(randomAccountNo("TXA"));
		long bId = createAccount(randomAccountNo("TXB"));

		deposit(aId, 1_000_000);
		withdraw(aId, 200_000, 200);
		remit(aId, bId, 100_000, 200);

		JsonNode listA = transactions(aId, 20);
		assertThat(listA.isArray()).isTrue();
		assertThat(listA.size()).isGreaterThanOrEqualTo(4);

		// 타입 존재 확인(구현에 따라 FEE/TRANSFER_OUT 순서는 같거나 바뀔 수 있어서 포함여부로 체크)
		String typesA = listA.toString();
		assertThat(typesA).contains("DEPOSIT");
		assertThat(typesA).contains("WITHDRAW");
		assertThat(typesA).contains("TRANSFER_OUT");
		assertThat(typesA).contains("FEE");

		// occurredAt 내림차순(최신순) 체크
		for (int i = 0; i < listA.size() - 1; i++) {
			Instant t1 = Instant.parse(listA.get(i).path("occurredAt").asText());
			Instant t2 = Instant.parse(listA.get(i + 1).path("occurredAt").asText());
			assertThat(!t1.isBefore(t2)).isTrue(); // t1 >= t2
		}

		JsonNode listB = transactions(bId, 20);
		assertThat(listB.toString()).contains("TRANSFER_IN");
	}

	// -------------------- 5) 계좌 삭제 --------------------
	@Test
	void delete_account_then_operations_return_inactive() throws Exception {
		long aId = createAccount(randomAccountNo("DEL"));

		deleteAccount(aId, 200);

		JsonNode err = json(deposit(aId, 1_000, 400));
		assertThat(err.path("code").asText()).isEqualTo("ACCOUNT_INACTIVE");
	}

	// -------------------- 6) 예외(중복/검증/없는 계좌) --------------------
	@Test
	void exceptions_duplicate_validation_not_found() throws Exception {
		String accNo = randomAccountNo("DUP");
		createAccount(accNo);

		JsonNode dup = json(createAccountExpectError(accNo, 400));
		assertThat(dup.path("code").asText()).isEqualTo("DUPLICATE_ACCOUNT_NO");

		long aId = createAccount(randomAccountNo("VAL"));
		JsonNode invalid = json(deposit(aId, 0, 400));
		assertThat(invalid.path("code").asText()).isEqualTo("VALIDATION_ERROR");

		JsonNode notFound = json(deposit(999999L, 1000, 404));
		assertThat(notFound.path("code").asText()).isEqualTo("ACCOUNT_NOT_FOUND");
	}

	// ===================== Helpers =====================

	private String randomAccountNo(String prefix) {
		return prefix + "-" + System.nanoTime();
	}

	private JsonNode json(String body) throws Exception {
		return om.readTree(body);
	}

	private long createAccount(String accountNo) throws Exception {
		String body = om.writeValueAsString(Map.of("accountNo", accountNo));
		MvcResult result = mvc.perform(post("/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(200);

		JsonNode root = json(result.getResponse().getContentAsString());
		return root.path("data").path("accountId").asLong();
	}

	private String createAccountExpectError(String accountNo, int expectedStatus) throws Exception {
		String body = om.writeValueAsString(Map.of("accountNo", accountNo));
		MvcResult result = mvc.perform(post("/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
		return result.getResponse().getContentAsString();
	}

	private String deposit(long accountId, long amount, int expectedStatus) throws Exception {
		String body = om.writeValueAsString(Map.of("amount", amount));
		MvcResult result = mvc.perform(post("/accounts/" + accountId + "/deposit")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
		return result.getResponse().getContentAsString();
	}

	private void deposit(long accountId, long amount) throws Exception {
		deposit(accountId, amount, 200);
	}

	private String withdraw(long accountId, long amount, int expectedStatus) throws Exception {
		String body = om.writeValueAsString(Map.of("amount", amount));
		MvcResult result = mvc.perform(post("/accounts/" + accountId + "/withdraw")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
		return result.getResponse().getContentAsString();
	}

	private String remit(long fromId, long toId, long amount, int expectedStatus) throws Exception {
		String body = om.writeValueAsString(Map.of(
			"fromAccountId", fromId,
			"toAccountId", toId,
			"amount", amount
		));
		MvcResult result = mvc.perform(post("/remittances")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
		return result.getResponse().getContentAsString();
	}

	private JsonNode transactions(long accountId, int size) throws Exception {
		MvcResult result = mvc.perform(get("/accounts/" + accountId + "/transactions")
				.param("size", String.valueOf(size)))
			.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(200);
		return json(result.getResponse().getContentAsString()).path("data");
	}

	private void deleteAccount(long accountId, int expectedStatus) throws Exception {
		MvcResult result = mvc.perform(delete("/accounts/" + accountId))
			.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
	}
}
