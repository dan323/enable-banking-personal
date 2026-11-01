package com.personal.banking.application;

import com.personal.banking.port.in.GetTransactionsUseCase;
import com.personal.banking.port.in.ListBanksUseCase;
import com.personal.banking.port.out.BankingProviderPort;
import com.personal.banking.domain.model.Bank;
import com.personal.banking.domain.model.TransactionPage;

import java.util.List;

public class BankingQueryService implements ListBanksUseCase, GetTransactionsUseCase {

    private final BankingProviderPort provider;

    public BankingQueryService(BankingProviderPort provider) {
        this.provider = provider;
    }

    @Override
    public List<Bank> listBanks(String country) {
        return provider.listBanks(country);
    }

    @Override
    public TransactionPage transactions(String accountUid, String dateFrom, String continuationKey) {
        return provider.transactions(accountUid, dateFrom, continuationKey);
    }
}
