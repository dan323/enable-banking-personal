package com.personal.banking.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** A balance of one account. {@code type} is the ISO 20022 balance type (CLBD, ITAV, ...). */
public record Balance(String type, BigDecimal amount, String currency) {

    // Preferred balance types, best first: booked, interim booked, expected, closing available...
    private static final List<String> PREFERENCE = List.of("CLBD", "ITBD", "XPCD", "CLAV", "ITAV");

    /** Picks the most meaningful balance: by type preference, otherwise the first one returned. */
    public static Optional<Balance> preferred(List<Balance> balances) {
        if (balances == null || balances.isEmpty()) return Optional.empty();
        for (String type : PREFERENCE) {
            for (Balance b : balances) {
                if (type.equals(b.type())) return Optional.of(b);
            }
        }
        return Optional.of(balances.get(0));
    }
}
