package com.personal.banking.port.out;

import com.personal.banking.domain.model.Balance;
import com.personal.banking.domain.model.Bank;
import com.personal.banking.domain.model.BankSession;
import com.personal.banking.domain.model.TransactionPage;

import java.net.URI;
import java.util.List;

/** READ-ONLY access to a PSD2 account-information provider. Nothing here can initiate payments. */
public interface BankingProviderPort {

    List<Bank> listBanks(String country);

    /** Starts the bank authorisation; returns the bank URL the user must visit. */
    URI startAuthorization(String bank, String country, String state);

    /** Exchanges the redirect code for a session (accounts are returned only once: store them). */
    BankSession createSession(String code);

    List<Balance> balances(String accountUid);

    TransactionPage transactions(String accountUid, String dateFrom, String continuationKey);
}
