package com.personal.banking.adapter.out.enablebanking;

import tools.jackson.databind.JsonNode;
import com.personal.banking.domain.model.Account;
import com.personal.banking.domain.model.Balance;
import com.personal.banking.domain.model.Bank;
import com.personal.banking.domain.model.BankSession;
import com.personal.banking.domain.model.Transaction;
import com.personal.banking.domain.model.TransactionPage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Translates Enable Banking JSON payloads into domain objects. */
final class EnableBankingMapper {

    private EnableBankingMapper() {}

    static List<Bank> toBanks(JsonNode response) {
        List<Bank> banks = new ArrayList<>();
        for (JsonNode b : response.path("aspsps")) {
            banks.add(new Bank(b.path("name").asText(), b.path("country").asText(null)));
        }
        return banks;
    }

    static BankSession toSession(JsonNode session) {
        List<Account> accounts = new ArrayList<>();
        for (JsonNode a : session.path("accounts")) {
            accounts.add(new Account(
                    a.path("uid").asText(),
                    a.path("account_id").path("iban").asText(null),
                    firstNonBlank(a.path("name").asText(null), a.path("details").asText(null), "Account"),
                    a.path("currency").asText("EUR")));
        }
        return new BankSession(
                session.path("session_id").asText(),
                session.path("access").path("valid_until").asText(null),
                accounts);
    }

    static List<Balance> toBalances(JsonNode response) {
        List<Balance> balances = new ArrayList<>();
        for (JsonNode b : response.path("balances")) {
            JsonNode amount = b.path("balance_amount");
            balances.add(new Balance(
                    b.path("balance_type").asText(),
                    new BigDecimal(amount.path("amount").asText("0")),
                    amount.path("currency").asText(null)));
        }
        return balances;
    }

    static TransactionPage toTransactionPage(JsonNode response) {
        List<Transaction> transactions = new ArrayList<>();
        for (JsonNode t : response.path("transactions")) {
            JsonNode amount = t.path("transaction_amount");
            List<String> remittance = new ArrayList<>();
            for (JsonNode r : t.path("remittance_information")) remittance.add(r.asText());
            transactions.add(new Transaction(
                    t.path("booking_date").asText(null),
                    t.path("value_date").asText(null),
                    new BigDecimal(amount.path("amount").asText("0")),
                    amount.path("currency").asText(null),
                    "DBIT".equals(t.path("credit_debit_indicator").asText())
                            ? Transaction.Direction.DEBIT : Transaction.Direction.CREDIT,
                    t.path("creditor").path("name").asText(null),
                    t.path("debtor").path("name").asText(null),
                    remittance));
        }
        return new TransactionPage(transactions, response.path("continuation_key").asText(null));
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return "";
    }
}
