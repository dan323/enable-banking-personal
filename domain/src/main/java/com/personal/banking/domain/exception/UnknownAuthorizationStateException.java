package com.personal.banking.domain.exception;

/** The callback carried a state we never issued (or already consumed): possible forged callback. */
public class UnknownAuthorizationStateException extends RuntimeException {
    public UnknownAuthorizationStateException() {
        super("Unknown authorization state");
    }
}
