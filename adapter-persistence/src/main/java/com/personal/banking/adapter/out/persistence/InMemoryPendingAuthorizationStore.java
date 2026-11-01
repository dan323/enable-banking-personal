package com.personal.banking.adapter.out.persistence;

import com.personal.banking.port.out.PendingAuthorizationPort;
import com.personal.banking.domain.model.PendingAuthorization;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPendingAuthorizationStore implements PendingAuthorizationPort {

    private final Map<String, PendingAuthorization> pending = new ConcurrentHashMap<>();

    @Override
    public void remember(String state, PendingAuthorization authorization) {
        pending.put(state, authorization);
    }

    @Override
    public Optional<PendingAuthorization> take(String state) {
        return Optional.ofNullable(pending.remove(state));
    }
}
