package com.personal.banking.domain.model;

import java.util.List;

public record TransactionPage(List<Transaction> transactions, String continuationKey) {}
