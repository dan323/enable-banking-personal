package com.personal.banking.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.personal.banking.domain.model.Transaction;
import com.personal.banking.domain.model.TransactionPage;

import java.util.List;

/** JSON shape consumed by the dashboard (snake_case, as the Enable Banking API names it). */
public record TransactionsResponse(
        List<TransactionDto> transactions,
        @JsonProperty("continuation_key") String continuationKey) {

    public record TransactionDto(
            @JsonProperty("booking_date") String bookingDate,
            @JsonProperty("value_date") String valueDate,
            @JsonProperty("transaction_amount") AmountDto transactionAmount,
            @JsonProperty("credit_debit_indicator") String creditDebitIndicator,
            PartyDto creditor,
            PartyDto debtor,
            @JsonProperty("remittance_information") List<String> remittanceInformation) {}

    public record AmountDto(String amount, String currency) {}

    public record PartyDto(String name) {}

    public static TransactionsResponse from(TransactionPage page) {
        return new TransactionsResponse(
                page.transactions().stream().map(TransactionsResponse::toDto).toList(),
                page.continuationKey());
    }

    private static TransactionDto toDto(Transaction t) {
        return new TransactionDto(
                t.bookingDate(),
                t.valueDate(),
                new AmountDto(t.amount().toPlainString(), t.currency()),
                t.direction() == Transaction.Direction.DEBIT ? "DBIT" : "CRDT",
                party(t.creditorName()),
                party(t.debtorName()),
                t.remittanceInformation());
    }

    private static PartyDto party(String name) {
        return name == null ? null : new PartyDto(name);
    }
}
