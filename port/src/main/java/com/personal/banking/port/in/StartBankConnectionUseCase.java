package com.personal.banking.port.in;

import java.net.URI;

public interface StartBankConnectionUseCase {
    /** Starts the authorisation and returns the bank URL the user must be redirected to. */
    URI start(String bank, String country);
}
