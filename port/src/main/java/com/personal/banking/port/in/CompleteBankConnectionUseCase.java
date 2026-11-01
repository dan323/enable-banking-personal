package com.personal.banking.port.in;

public interface CompleteBankConnectionUseCase {
    /**
     * Finishes the authorisation after the bank redirects back.
     *
     * @return the name of the bank that was connected
     * @throws com.personal.banking.domain.exception.UnknownAuthorizationStateException if state is unknown
     */
    String complete(String code, String state);
}
