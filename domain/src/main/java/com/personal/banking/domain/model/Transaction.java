package com.personal.banking.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record Transaction(String bookingDate, String valueDate, BigDecimal amount, String currency,
                          Direction direction, String creditorName, String debtorName,
                          List<String> remittanceInformation) {

    public enum Direction { CREDIT, DEBIT }
}
