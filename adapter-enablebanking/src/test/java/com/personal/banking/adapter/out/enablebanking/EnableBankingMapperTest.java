package com.personal.banking.adapter.out.enablebanking;

import tools.jackson.databind.ObjectMapper;
import com.personal.banking.domain.model.Balance;
import com.personal.banking.domain.model.BankSession;
import com.personal.banking.domain.model.Transaction;
import com.personal.banking.domain.model.TransactionPage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EnableBankingMapperTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void mapsBalances() throws Exception {
        List<Balance> balances = EnableBankingMapper.toBalances(mapper.readTree("""
                {"balances":[
                  {"balance_type":"CLBD","balance_amount":{"currency":"EUR","amount":"100.50"}},
                  {"balance_type":"ITAV","balance_amount":{"amount":"5"}}]}"""));

        assertEquals(2, balances.size());
        assertEquals("CLBD", balances.get(0).type());
        assertEquals(0, new BigDecimal("100.50").compareTo(balances.get(0).amount()));
        assertEquals("EUR", balances.get(0).currency());
        assertNull(balances.get(1).currency());
    }

    @Test
    void mapsSessionAccountsWithNameFallback() throws Exception {
        BankSession session = EnableBankingMapper.toSession(mapper.readTree("""
                {"session_id":"s1","access":{"valid_until":"2027-01-01T00:00:00Z"},
                 "accounts":[
                   {"uid":"a1","account_id":{"iban":"ES00"},"name":"Main","currency":"EUR"},
                   {"uid":"a2","details":"Savings"}]}"""));

        assertEquals("s1", session.sessionId());
        assertEquals("2027-01-01T00:00:00Z", session.validUntil());
        assertEquals("Main", session.accounts().get(0).name());
        assertEquals("ES00", session.accounts().get(0).iban());
        assertEquals("Savings", session.accounts().get(1).name());
        assertEquals("EUR", session.accounts().get(1).currency());
    }

    @Test
    void mapsTransactions() throws Exception {
        TransactionPage page = EnableBankingMapper.toTransactionPage(mapper.readTree("""
                {"continuation_key":"next","transactions":[
                  {"booking_date":"2026-10-01","credit_debit_indicator":"DBIT",
                   "transaction_amount":{"amount":"12.30","currency":"EUR"},
                   "creditor":{"name":"Shop"},"remittance_information":["Groceries"]}]}"""));

        assertEquals("next", page.continuationKey());
        Transaction t = page.transactions().get(0);
        assertEquals(Transaction.Direction.DEBIT, t.direction());
        assertEquals("Shop", t.creditorName());
        assertNull(t.debtorName());
        assertEquals(List.of("Groceries"), t.remittanceInformation());
    }
}
