package com.personal.banking.application;

import com.personal.banking.port.in.CompleteBankConnectionUseCase;
import com.personal.banking.port.in.DisconnectBankUseCase;
import com.personal.banking.port.in.StartBankConnectionUseCase;
import com.personal.banking.port.out.BankingProviderPort;
import com.personal.banking.port.out.ConnectionRepositoryPort;
import com.personal.banking.port.out.PendingAuthorizationPort;
import com.personal.banking.domain.exception.UnknownAuthorizationStateException;
import com.personal.banking.domain.model.BankSession;
import com.personal.banking.domain.model.Connection;
import com.personal.banking.domain.model.PendingAuthorization;

import java.net.URI;
import java.util.UUID;

/** Bank authorisation flow: start -> user approves at the bank -> complete. */
public class BankConnectionService
        implements StartBankConnectionUseCase, CompleteBankConnectionUseCase, DisconnectBankUseCase {

    private final BankingProviderPort provider;
    private final ConnectionRepositoryPort connections;
    private final PendingAuthorizationPort pending;

    public BankConnectionService(BankingProviderPort provider, ConnectionRepositoryPort connections,
                                 PendingAuthorizationPort pending) {
        this.provider = provider;
        this.connections = connections;
        this.pending = pending;
    }

    @Override
    public URI start(String bank, String country) {
        String state = UUID.randomUUID().toString();
        pending.remember(state, new PendingAuthorization(bank, country));
        return provider.startAuthorization(bank, country, state);
    }

    @Override
    public String complete(String code, String state) {
        PendingAuthorization auth = pending.take(state).orElseThrow(UnknownAuthorizationStateException::new);
        BankSession session = provider.createSession(code);
        connections.save(new Connection(session.sessionId(), auth.bank(), auth.country(),
                session.validUntil(), session.accounts()));
        return auth.bank();
    }

    @Override
    public void disconnect(String sessionId) {
        connections.remove(sessionId);
    }
}
