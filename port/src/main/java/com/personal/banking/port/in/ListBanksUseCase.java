package com.personal.banking.port.in;

import com.personal.banking.domain.model.Bank;

import java.util.List;

public interface ListBanksUseCase {
    List<Bank> listBanks(String country);
}
