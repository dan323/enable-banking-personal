package com.personal.banking.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Combined view: every account of every connected bank, plus totals per currency. */
public interface GetPortfolioUseCase {

    Summary summary();

    record AccountView(String uid, String name, String iban, String currency,
                       BigDecimal balance, String balanceType, String error) {}

    record BankView(String sessionId, String bank, String country, String validUntil,
                    List<AccountView> accounts) {}

    record Summary(List<BankView> banks, Map<String, BigDecimal> totalsByCurrency) {}
}
