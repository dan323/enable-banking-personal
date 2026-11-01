package com.personal.banking.port.in;

import com.personal.banking.domain.model.TransactionPage;

public interface GetTransactionsUseCase {
    TransactionPage transactions(String accountUid, String dateFrom, String continuationKey);
}
