package com.personal.banking.application;

import com.personal.banking.port.in.GetPortfolioUseCase;
import com.personal.banking.port.out.BankingProviderPort;
import com.personal.banking.port.out.ConnectionRepositoryPort;
import com.personal.banking.domain.model.Account;
import com.personal.banking.domain.model.Balance;
import com.personal.banking.domain.model.Connection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PortfolioService implements GetPortfolioUseCase {

    private final ConnectionRepositoryPort connections;
    private final BankingProviderPort provider;

    public PortfolioService(ConnectionRepositoryPort connections, BankingProviderPort provider) {
        this.connections = connections;
        this.provider = provider;
    }

    @Override
    public Summary summary() {
        List<BankView> banks = new ArrayList<>();
        Map<String, BigDecimal> totals = new LinkedHashMap<>();

        for (Connection c : connections.findAll()) {
            List<AccountView> accounts = new ArrayList<>();
            for (Account a : c.accounts()) {
                AccountView view = loadAccount(a);
                accounts.add(view);
                if (view.balance() != null) {
                    totals.merge(view.currency(), view.balance(), BigDecimal::add);
                }
            }
            banks.add(new BankView(c.sessionId(), c.bank(), c.country(), c.validUntil(), accounts));
        }
        return new Summary(banks, totals);
    }

    private AccountView loadAccount(Account a) {
        try {
            Optional<Balance> chosen = Balance.preferred(provider.balances(a.uid()));
            if (chosen.isEmpty()) {
                return new AccountView(a.uid(), a.name(), a.iban(), a.currency(), null, null, "No balance returned");
            }
            Balance b = chosen.get();
            return new AccountView(a.uid(), a.name(), a.iban(),
                    b.currency() != null ? b.currency() : a.currency(),
                    b.amount(), b.type(), null);
        } catch (Exception e) {
            return new AccountView(a.uid(), a.name(), a.iban(), a.currency(), null, null, e.getMessage());
        }
    }
}
