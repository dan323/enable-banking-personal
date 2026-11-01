package com.personal.banking.port.out;

import com.personal.banking.domain.model.PendingAuthorization;

import java.util.Optional;

/** In-flight authorisations keyed by state. Protects against forged callbacks. */
public interface PendingAuthorizationPort {

    void remember(String state, PendingAuthorization pending);

    /** Returns and forgets the authorisation for this state (single use). */
    Optional<PendingAuthorization> take(String state);
}
