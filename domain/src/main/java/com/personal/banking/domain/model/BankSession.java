package com.personal.banking.domain.model;

import java.util.List;

/** What the banking provider returns once the user has approved access at their bank. */
public record BankSession(String sessionId, String validUntil, List<Account> accounts) {}
