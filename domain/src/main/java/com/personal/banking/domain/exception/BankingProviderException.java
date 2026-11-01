package com.personal.banking.domain.exception;

/** The banking provider (e.g. Enable Banking) rejected or failed a request. */
public class BankingProviderException extends RuntimeException {

    private final String detail;

    public BankingProviderException(String message, String detail, Throwable cause) {
        super(message, cause);
        this.detail = detail;
    }

    /** Raw error body from the provider, if any. */
    public String detail() {
        return detail;
    }
}
