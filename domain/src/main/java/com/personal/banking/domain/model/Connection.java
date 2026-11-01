package com.personal.banking.domain.model;

import java.util.List;

/** An authorised link to one bank, with the accounts the user granted access to. */
public record Connection(String sessionId, String bank, String country,
                         String validUntil, List<Account> accounts) {}
