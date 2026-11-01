package com.personal.banking.domain.model;

/** An authorisation that was started but not yet confirmed by the bank's redirect. */
public record PendingAuthorization(String bank, String country) {}
