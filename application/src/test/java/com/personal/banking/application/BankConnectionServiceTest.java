package com.personal.banking.application;

import com.personal.banking.port.out.BankingProviderPort;
import com.personal.banking.port.out.ConnectionRepositoryPort;
import com.personal.banking.port.out.PendingAuthorizationPort;
import com.personal.banking.domain.exception.UnknownAuthorizationStateException;
import com.personal.banking.domain.model.Account;
import com.personal.banking.domain.model.BankSession;
import com.personal.banking.domain.model.Connection;
import com.personal.banking.domain.model.PendingAuthorization;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BankConnectionServiceTest {

    private final BankingProviderPort provider = Mockito.mock(BankingProviderPort.class);
    private final ConnectionRepositoryPort connections = Mockito.mock(ConnectionRepositoryPort.class);
    private final PendingAuthorizationPort pending = Mockito.mock(PendingAuthorizationPort.class);
    private final BankConnectionService service = new BankConnectionService(provider, connections, pending);

    @Test
    void startRemembersStateAndReturnsBankUrl() {
        URI bankUrl = URI.create("https://bank.example/auth");
        Mockito.when(provider.startAuthorization(Mockito.eq("Sabadell"), Mockito.eq("ES"), Mockito.anyString()))
                .thenReturn(bankUrl);

        URI result = service.start("Sabadell", "ES");

        assertEquals(bankUrl, result);
        ArgumentCaptor<String> state = ArgumentCaptor.forClass(String.class);
        Mockito.verify(pending).remember(state.capture(), Mockito.eq(new PendingAuthorization("Sabadell", "ES")));
        Mockito.verify(provider).startAuthorization("Sabadell", "ES", state.getValue());
    }

    @Test
    void completeSavesConnectionForKnownState() {
        Mockito.when(pending.take("st")).thenReturn(Optional.of(new PendingAuthorization("Sabadell", "ES")));
        List<Account> accounts = List.of(new Account("a1", "ES00", "Cuenta", "EUR"));
        Mockito.when(provider.createSession("code")).thenReturn(new BankSession("sess", "2027-01-01", accounts));

        String bank = service.complete("code", "st");

        assertEquals("Sabadell", bank);
        Mockito.verify(connections).save(new Connection("sess", "Sabadell", "ES", "2027-01-01", accounts));
    }

    @Test
    void completeRejectsUnknownStateWithoutCallingTheProvider() {
        Mockito.when(pending.take("forged")).thenReturn(Optional.empty());

        assertThrows(UnknownAuthorizationStateException.class, () -> service.complete("code", "forged"));

        Mockito.verifyNoInteractions(provider, connections);
    }
}
